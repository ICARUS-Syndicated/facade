#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Minecraft 数据包自动部署工具 (使用 mcsmapi SDK + 原版上传)
功能：
1. 打包 data 文件夹和 pack.mcmeta 为 data.zip
2. 通过 mcsmapi SDK 管理服务端文件，通过 REST API 上传
3. 通过 RCON 协议发送重载命令

用法：
    python minecraft_datapack_deploy.py         # 普通模式（智能备份）
    python minecraft_datapack_deploy.py --force # 强制备份模式
    python minecraft_datapack_deploy.py --skip-server-backup  # 跳过服务端备份
"""

import os
import sys
import json
import zipfile
import requests
import argparse
from mcrcon import MCRcon
from mcsmapi import MCSMAPI

# 设置标准输出编码为 UTF-8
if sys.platform == 'win32':
    import codecs
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')


def load_config():
    """从 config.json 加载配置信息"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    config_path = os.path.join(script_dir, 'config.json')
    
    if not os.path.exists(config_path):
        raise FileNotFoundError(f"配置文件不存在: {config_path}")
    
    with open(config_path, 'r', encoding='utf-8') as f:
        return json.load(f)


def backup_old_zip(base_dir):
    """
    备份旧的 zip 文件
    - 如果存在 data-old.zip，删除它
    - 如果存在 data.zip，重命名为 data-old.zip
    """
    zip_path = os.path.join(base_dir, 'data.zip')
    old_zip_path = os.path.join(base_dir, 'data-old.zip')
    
    if os.path.exists(old_zip_path):
        os.remove(old_zip_path)
        print(f"[OK] 已删除旧备份: data-old.zip")
    
    if os.path.exists(zip_path):
        os.rename(zip_path, old_zip_path)
        print(f"[OK] 已备份旧文件: data.zip → data-old.zip")


def create_zip(base_dir, source_dir, source_file, output_zip):
    """
    将指定文件夹和文件打包成zip
    """
    with zipfile.ZipFile(output_zip, 'w', zipfile.ZIP_DEFLATED) as zipf:
        data_path = os.path.join(base_dir, source_dir)
        if os.path.exists(data_path):
            for root, dirs, files in os.walk(data_path):
                for file in files:
                    file_path = os.path.join(root, file)
                    arcname = os.path.join(source_dir, os.path.relpath(file_path, data_path))
                    zipf.write(file_path, arcname)
            print(f"[OK] 已添加文件夹: {source_dir}/")
        else:
            print(f"[WARN] 警告: 文件夹 {source_dir}/ 不存在")
        
        mcmeta_path = os.path.join(base_dir, source_file)
        if os.path.exists(mcmeta_path):
            zipf.write(mcmeta_path, source_file)
            print(f"[OK] 已添加文件: {source_file}")
        else:
            print(f"[WARN] 警告: 文件 {source_file} 不存在")
    
    print(f"[OK] 打包完成: {output_zip}")


def normalize_path(path: str) -> str:
    """
    规范化路径，确保格式正确
    - 移除开头的 ./ 或 / 
    - 确保不以 / 开头（相对路径）
    """
    # 移除开头的 ./ 或 /
    while path.startswith('./') or path.startswith('/'):
        if path.startswith('./'):
            path = path[2:]
        elif path.startswith('/'):
            path = path[1:]
    return path


def file_exists(mcsm: MCSMAPI, daemon_id: str, instance_uuid: str, file_path: str) -> bool:
    """
    检查服务端文件是否存在
    """
    try:
        # 尝试获取文件列表来检查文件是否存在
        dir_path = os.path.dirname(file_path) or "."
        file_list = mcsm.file.show(
            daemonId=daemon_id,
            uuid=instance_uuid,
            target=dir_path
        )
        file_name = os.path.basename(file_path)
        for item in file_list.data:
            if item.name == file_name:
                return True
        return False
    except Exception:
        return False


def backup_server_zip(mcsm: MCSMAPI, daemon_id: str, instance_uuid: str, 
                      target_path: str, force: bool = False) -> bool:
    """
    在服务端备份 data.zip -> data-old.zip (使用 mcsmapi SDK)
    返回是否成功完成备份操作
    """
    filename = 'data.zip'
    backup_name = 'data-old.zip'
    
    # 规范化路径
    target_path = normalize_path(target_path)
    
    # 构建完整文件路径
    source_file = f"{target_path}/{filename}" if target_path else filename
    backup_file = f"{target_path}/{backup_name}" if target_path else backup_name
    
    if force:
        print(f"[INFO] [强制备份] 正在备份服务端 {source_file} -> {backup_file}...")
    else:
        print(f"[INFO] 正在检查服务端文件...")
    
    # 首先检查文件是否存在
    if not file_exists(mcsm, daemon_id, instance_uuid, source_file):
        print(f"[INFO] 服务端不存在 {source_file}，跳过备份")
        return True
    
    print(f"[INFO] 发现服务端 {source_file}，准备备份...")
    
    try:
        # 第一步：尝试删除旧的 data-old.zip（如果存在）
        try:
            if file_exists(mcsm, daemon_id, instance_uuid, backup_file):
                mcsm.file.delete(
                    daemonId=daemon_id,
                    uuid=instance_uuid,
                    targets=[backup_file]
                )
                print(f"[INFO] 已删除旧的 {backup_name}")
        except Exception as e:
            print(f"[DEBUG] 删除旧备份失败（可能不存在）: {e}")
            pass
        
        # 第二步：尝试重命名 data.zip -> data-old.zip
        copy_map = {source_file: backup_file}
        result = mcsm.file.move(
            daemonId=daemon_id,
            uuid=instance_uuid,
            copy_map=copy_map
        )
        
        if result:
            print(f"[OK] 服务端备份成功: {filename} -> {backup_name}")
            return True
        else:
            raise Exception("move 返回 False")
            
    except Exception as e:
        error_msg = str(e)
        
        # 检查是否是路径权限错误
        if "Illegal access path" in error_msg:
            print(f"[WARN] 服务端路径访问被拒绝")
            print(f"[INFO] 可能原因：")
            print(f"      1. 路径 '{target_path}' 不在实例允许访问的范围内")
            print(f"      2. 实例配置了文件系统访问限制")
            print(f"      3. 路径格式不正确（尝试修改 config.json 中的 target_path）")
        elif "500" in error_msg:
            print(f"[WARN] 服务端操作失败 (500): {error_msg}")
        else:
            print(f"[WARN] 服务端备份操作异常: {e}")
        
        if force:
            print(f"[FAIL] 强制备份模式下备份失败，停止部署")
            return False
        else:
            print(f"[INFO] 非强制模式，继续上传新文件（可能覆盖旧文件）...")
            return True


def upload_via_mcsm(mcsm: MCSMAPI, config: dict, zip_path: str, 
                    force_backup: bool = False, skip_server_backup: bool = False):
    """
    通过 mcsmapi SDK 管理文件，通过 REST API 上传文件
    """
    mcsm_config = config.get('mcsm', {})
    
    api_url = mcsm_config.get('api_url', '')
    api_key = mcsm_config.get('api_key', '')
    daemon_id = mcsm_config.get('daemon_id', '')
    instance_uuid = mcsm_config.get('instance_uuid', '')
    target_path = mcsm_config.get('target_path', 'world/datapacks')
    
    if not all([api_url, api_key, daemon_id, instance_uuid]):
        raise ValueError("MCSM 配置不完整，请检查 config.json")
    
    filename = os.path.basename(zip_path)
    
    # 规范化路径
    target_path = normalize_path(target_path)
    
    # 第零步：备份服务端已存在的 data.zip（如果未跳过）
    if skip_server_backup:
        print(f"[INFO] 跳过服务端备份")
    else:
        backup_success = backup_server_zip(mcsm, daemon_id, instance_uuid, target_path, force_backup)
        if not backup_success and force_backup:
            raise Exception("强制备份失败，停止部署")
    
    # 第一步：获取上传配置
    print(f"[INFO] 正在获取上传配置...")
    upload_config_url = f"{api_url}/api/files/upload"
    params = {
        'apikey': api_key,
        'daemonId': daemon_id,
        'uuid': instance_uuid,
        'upload_dir': target_path
    }
    
    response = requests.post(upload_config_url, params=params, timeout=30)
    response.raise_for_status()
    result = response.json()
    
    if result.get('status') != 200:
        raise Exception(f"获取上传配置失败: {result.get('data', '未知错误')}")
    
    upload_data = result.get('data', {})
    password = upload_data.get('password')
    addr = upload_data.get('addr')
    
    if not password or not addr:
        raise Exception("上传配置返回不完整")
    
    print(f"[OK] 获取上传配置成功")
    
    # 第二步：上传文件到 Daemon
    if addr.startswith('wss://'):
        daemon_url = addr.replace('wss://', 'https://')
    elif addr.startswith('ws://'):
        daemon_url = addr.replace('ws://', 'http://')
    else:
        protocol = api_url.split('://')[0]
        daemon_url = f"{protocol}://{addr}"
    
    upload_url = f"{daemon_url}/upload/{password}"
    
    print(f"[INFO] 正在上传 {filename} 到 {target_path}...")
    
    with open(zip_path, 'rb') as f:
        files = {'file': (filename, f, 'application/zip')}
        response = requests.post(upload_url, files=files, timeout=120)
        response.raise_for_status()
        print(f"[OK] 文件上传成功")


def send_rcon_command(config: dict, command: str):
    """
    通过 RCON 协议发送命令
    """
    rcon_config = config.get('rcon', {})
    
    host = rcon_config.get('host', 'localhost')
    port = rcon_config.get('port', 25575)
    password = rcon_config.get('password', '')
    
    if not password:
        raise ValueError("RCON 密码未配置，请检查 config.json")
    
    try:
        print(f"[INFO] 正在连接 RCON {host}:{port}...")
        with MCRcon(host, password, port=port) as mcr:
            print(f"[INFO] 发送命令: {command}")
            response = mcr.command(command)
            print(f"[OK] 服务器响应: {response}")
    except Exception as e:
        raise Exception(f"RCON 连接失败: {e}")


def init_mcsm_client(config: dict) -> MCSMAPI:
    """
    初始化 MCSMAPI 客户端并登录
    """
    mcsm_config = config.get('mcsm', {})
    api_url = mcsm_config.get('api_url', '')
    api_key = mcsm_config.get('api_key', '')
    
    if not api_url:
        raise ValueError("MCSM API URL 未配置，请检查 config.json")
    
    if not api_key:
        raise ValueError("MCSM API Key 未配置，请检查 config.json")
    
    mcsm = MCSMAPI(api_url)
    mcsm.login_with_apikey(api_key)
    print(f"[OK] MCSManager 登录成功: {api_url}")
    
    return mcsm


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='Minecraft 数据包自动部署工具 (mcsmapi + 原版上传)')
    parser.add_argument('--force', '-f', action='store_true',
                        help='强制备份模式（备份失败时停止部署）')
    parser.add_argument('--skip-server-backup', '-s', action='store_true',
                        help='跳过服务端备份（直接上传覆盖）')
    args = parser.parse_args()
    
    print("=" * 50)
    print("Minecraft 数据包自动部署工具 (mcsmapi + 原版上传)")
    if args.force:
        print("[强制备份模式]")
    elif args.skip_server_backup:
        print("[跳过服务端备份模式]")
    print("=" * 50)
    
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    try:
        # 第一步：加载配置
        print("\n[1/5] 加载配置文件...")
        config = load_config()
        print("[OK] 配置加载成功")
        
        # 第二步：初始化 MCSMAPI 客户端
        print("\n[2/5] 初始化 MCSManager 连接...")
        mcsm = init_mcsm_client(config)
        
        # 第三步：备份旧文件并打包
        print("\n[3/5] 备份旧文件并打包数据包...")
        backup_old_zip(base_dir)
        zip_path = os.path.join(base_dir, 'data.zip')
        create_zip(base_dir, 'data', 'pack.mcmeta', zip_path)
        
        # 第四步：上传文件
        print("\n[4/5] 上传数据包...")
        upload_via_mcsm(mcsm, config, zip_path, 
                        force_backup=args.force, 
                        skip_server_backup=args.skip_server_backup)
        
        # 第五步：发送 RCON 命令
        print("\n[5/5] 发送重载命令...")
        if args.skip_server_backup:
            print("[INFO] 跳过备份模式，等待 1 秒确保文件写入...")
            import time
            time.sleep(1)
        send_rcon_command(config, 'recipeaddon reload')
        
        print("\n" + "=" * 50)
        print("[OK] 部署完成！")
        print("=" * 50)
        
    except FileNotFoundError as e:
        print(f"\n[FAIL] 文件错误: {e}")
    except requests.RequestException as e:
        print(f"\n[FAIL] 网络请求错误: {e}")
    except Exception as e:
        print(f"\n[FAIL] 错误: {e}")


if __name__ == '__main__':
    main()
