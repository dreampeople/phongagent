# 若要使用DeepSeek来分析Word文件并生成分析报告，可按以下步骤操作：
# 1. 安装所需库：python-docx用于读取Word文件，可通过`pip install python-docx`安装；
#    若要使用DeepSeek API，需按照其官方文档配置环境。
# 2. 读取Word文件内容。
# 3. 调用DeepSeek API进行分析。
# 4. 生成分析报告。

import docx
import requests
import mammoth

def convert_word_to_markdown(file_path):
    with open(file_path, "rb") as docx_file:
        result = mammoth.convert_to_markdown(docx_file)
        markdown = result.value
        return markdown

# 读取Word文件内容
def read_word_file(file_path):
    doc = docx.Document(file_path)
    full_text = []
    for para in doc.paragraphs:
        full_text.append(para.text)
    return '\n'.join(full_text)

# 调用DeepSeek API进行分析
def analyze_with_deepseek(text, api_key):
    # 假设DeepSeek API的请求URL
    api_url = "https://api.deepseek.com/analysis"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }
    data = {
        "text": text
    }
    response = requests.post(api_url, headers=headers, json=data)
    if response.status_code == 200:
        return response.json()
    else:
        return None

# 生成分析报告
def generate_report(analysis_result):
    report = "分析报告：\n"
    # 这里根据实际的分析结果结构来生成报告
    if analysis_result:
        for key, value in analysis_result.items():
            report += f"{key}: {value}\n"
    else:
        report += "分析失败，请检查API调用。"
    return report

# 主函数
def main():
    file_path = "your_word_file.docx"
    api_key = "your_api_key"

    # 读取Word文件
    text = read_word_file(file_path)

    # 调用DeepSeek API进行分析
    analysis_result = analyze_with_deepseek(text, api_key)

    # 生成分析报告
    report = generate_report(analysis_result)

    # 打印报告
    print(report)

if __name__ == "__main__":
    main()
