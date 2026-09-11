# Minecraft 数据包自动部署工具 (mcsmapi 版本)

使用 [mcsmapi](https://pypi.org/project/mcsmapi/) SDK 实现的数据包自动上传和重载工具。

## 功能

执行后会自动：
1. 打包 `data/` 文件夹和 `pack.mcmeta` 为 `data.zip`
2. 通过 **mcsmapi SDK** 管理服务端文件（删除旧备份、重命名备份）
3. 通过 REST API 上传到 MCSManager 指定服务器目录
4. 通过 RCON 协议发送重载配方命令

> 添加配方使用的插件为 [Recipe-Addoner](https://github.com/RWPteam/Recipe-Addoner)

---

## 安装依赖

```bash
pip install -r requirements.txt
```

---

## 配置

首次运行前，请填写 `config.json` 中的空缺项：

```json
{
    "mcsm": {
        "api_url": "http://your-mcsm-panel.com",
        "api_key": "your-api-key",
        "daemon_id": "your-daemon-id",
        "instance_uuid": "your-instance-uuid",
        "target_path": "world/datapacks"
    },
    "rcon": {
        "host": "your-server-host",
        "port": 25575,
        "password": "your-rcon-password"
    }
}
```

### 配置项说明

| 配置项 | 说明 |
|--------|------|
| `mcsm.api_url` | MCSManager 面板地址（必须包含 http:// 或 https://） |
| `mcsm.api_key` | API 密钥，参见 [API 使用教程](https://docs.mcsmanager.com/zh_cn/apis/get_apikey.html) |
| `mcsm.daemon_id` | 守护进程 ID |
| `mcsm.instance_uuid` | 实例 UUID |
| `mcsm.target_path` | 数据包上传目标路径（**相对于实例工作目录的相对路径**，如 `world/datapacks`） |
| `rcon.host` | RCON 主机地址 |
| `rcon.port` | RCON 端口（查阅 server.properties） |
| `rcon.password` | RCON 密码（查阅 server.properties） |

### ⚠️ 重要提示

**`target_path` 必须是相对路径！**

- ✅ 正确: `"target_path": "world/datapacks"`
- ❌ 错误: `"target_path": "/world/datapacks"`
- ❌ 错误: `"target_path": "./world/datapacks"`

**[!] 请勿将 `config.json` 发送给其他人**

---

## 使用方法

```bash
# 普通模式（智能备份）
python minecraft_datapack_deploy.py

# 强制备份模式（备份失败时停止部署）
python minecraft_datapack_deploy.py --force

# 跳过服务端备份（直接上传覆盖）
python minecraft_datapack_deploy.py --skip-server-backup
```

---

## 技术栈

- [mcsmapi](https://pypi.org/project/mcsmapi/) - MCSManager Python SDK（用于文件管理）
- [requests](https://pypi.org/project/requests/) - HTTP 库（用于文件上传）
- [mcrcon](https://pypi.org/project/mcrcon/) - RCON 协议客户端
- Python 3.10+

---

## 故障排除

### 1. Illegal access path（非法访问路径）

**症状**: 
```
[WARN] 服务端路径访问被拒绝
[INFO] 可能原因：
      1. 路径 'xxx' 不在实例允许访问的范围内
      2. 实例配置了文件系统访问限制
      3. 路径格式不正确
```

**解决**:
1. 检查 `target_path` 是否为相对路径（如 `world/datapacks`）
2. 在 MCSManager 面板中检查实例的"文件管理"是否能看到该路径
3. 尝试使用 `--skip-server-backup` 参数跳过备份直接上传
4. 检查 MCSManager 实例配置中的文件系统白名单设置

### 2. 路径错误：Cannot move to subdirectory

**症状**: `Error: Cannot move 'xxx' to a subdirectory of itself`

**解决**: 确保 `target_path` 不以 `/` 开头

### 3. 上传失败：无法解析 'wss'

**症状**: `HTTPConnectionPool(host='wss', port=80): Max retries exceeded`

**解决**: 这是 SDK bug，本项目已使用原版上传方式修复。请确保使用的是最新版本代码。

### 4. RCON 连接失败

**症状**: `RCON 连接失败`

**解决**: 
- 检查 `server.properties` 中 `enable-rcon` 是否为 `true`
- 检查 RCON 端口和密码是否正确
- 检查防火墙是否允许 RCON 端口连接

---

## 实现细节

本项目采用**混合方案**：

| 功能 | 使用的库 | 原因 |
|------|----------|------|
| 文件删除、移动 | mcsmapi SDK | SDK 封装良好，调用简洁 |
| 文件上传 | requests (REST API) | mcsmapi SDK 的 `upload` 方法存在 [bug](https://github.com/molanp/mcsmapi/issues)，无法正确处理 `wss://` 地址 |

### mcsmapi SDK 的 Bug

SDK 的 `upload` 方法在处理 `wss://` 地址时，会构造错误的 URL：
```python
# SDK 代码 (file.py)
protocol = Request.mcsm_url.split("://")[0]  # "http"
base_url = urllib.parse.urljoin(f"{protocol}://{result.addr}", "upload")
# 当 result.addr = "wss://mc.example.com:24444"
# 结果: "http://wss://mc.example.com:24444/upload"  <-- 错误的 URL！
```

因此本项目使用原版 REST API 进行文件上传，直到 SDK 修复此问题。
