import os
import logging
import copy
import json
import shutil
# Logging configuration
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
# Mcmeta configuration
mcMeta = {
    "pack":{
        "description": "ResourcePack Generator by Annieawa v1.0",
        "pack_format": 55,
        "supported_formats": [34, 64],
        "min_format": 34,
        "max_format": [69, 0]
    }
}
# Json item data configuration
itemData = {
  "model": {
    "type": "model",
    "model": "ident:item/name"
  }
}
# Model data configuration
handheldList = ["sword","axe","pickaxe","hoe","spade","mace"]
modelTypes = ["generated","handheld"]
modelRoot = "minecraft:item/"
modelData = {
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "ident:item/name"
  }
}

dir = input("请输入执行路径:")
if os.path.isdir(dir):
    current_dir = dir
else:
    current_dir = os.getcwd()
    print(f"路径不存在！使用当前路径{current_dir}")

# get png files
png_files = [f for f in os.listdir(current_dir) 
             if os.path.isfile(os.path.join(current_dir,f)) and f.lower().endswith('.png')]

clean_names = [os.path.splitext(filename)[0] for filename in png_files]
lenth = len(clean_names)

logging.info(f"在目录下找到{lenth}张图片:{clean_names}")
output_dir = ""
dir2 = ""
while os.path.isdir(dir2) == False:
    dir2 = input("请选择输出路径:")

    if os.path.isdir(dir2):
        output_dir = dir2
    else:
        logging.error(f"路径不存在！")

ident = input("输入命名空间id:")
createType = int(input("选择创建模式:(1)新建,(2)附加"))
os.chdir(output_dir)
output_pointer = ""
if createType != 2:
    logging.info("开始创建路径...")
    try:
        os.makedirs(os.path.join("assets",ident), exist_ok=True)
        logging.info("路径创建成功！")
    except Exception as e:
        logging.error("路径创建时出错:",e)
    
    output_pointer = os.path.join(output_dir,"assets",ident)
    os.chdir(output_pointer)
    logging.info("创建文件路径...")
    try:
        os.mkdir("items")
        os.makedirs(os.path.join("models","item"), exist_ok=True)
        os.makedirs(os.path.join("textures","item"), exist_ok=True)
        logging.info("文件路径创建完成！")
    except Exception as e:
        logging.error("文件路径创建时出错:",e)
else:
    output_pointer = os.path.join(output_dir,"assets",ident)

# Starting json Dumping(items)
os.chdir(os.path.join(output_pointer,"items"))
for t in clean_names:
    if t != "pack":
        itemData["model"]["model"] = ident + ":item/" + t
        with open(t+".json", 'w', encoding='utf-8') as f:
            json.dump(itemData, f, ensure_ascii=False, indent=4)
        logging.info(f"文件{t}.json(item)创建完成。")
        
os.chdir(os.path.join(output_pointer,"models","item"))
msData = copy.deepcopy(modelData)
for t in clean_names:
    if t != "pack":
        if t.split("_")[-1] in handheldList:
            msData["parent"] = modelRoot + modelTypes[1]
        else:
            msData["parent"] = modelRoot + modelTypes[0]
        msData["textures"]["layer0"] = ident + ":item/" + t
        with open(t+".json", 'w', encoding='utf-8') as f:
            json.dump(msData, f, ensure_ascii=False, indent=4)
        logging.info(f"文件{t}.json(model)创建完成。")
# Starting png copying
output_pointer = os.path.join(output_pointer,"textures","item")
for t in png_files:
    if t != "pack.png":
        shutil.copy(os.path.join(current_dir,t), output_pointer)
        logging.info(f"图片文件{t}已复制进资源包。")
    else:
        shutil.copy(os.path.join(current_dir,t), output_dir)
        logging.info(f"图片文件{t}已作为资源包封面。")
        
# Starting mcmeta dumping
os.chdir(output_dir)
with open("pack.mcmeta", 'w', encoding='utf-8') as f:
    json.dump(mcMeta, f, ensure_ascii=False, indent=4)
logging.info(f"mcmeta文件已写入")

logging.info(f"资源包{ident}已创建完成！")
a = input("...")
