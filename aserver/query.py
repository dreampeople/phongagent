# 要使用DeepSeek对Word文档进行分析并生成分析报告，你可以使用以下步骤：
# 1. 安装必要的库：`python-docx`用于读取Word文档，`openai`用于与DeepSeek API交互。
# 2. 读取Word文档内容,转成markdown格式。
# 3. 调用DeepSeek API进行分析。
# 4. 生成分析报告。

# 安装必要的库
# pip install python-docx openai mammoth

#import docx
import mammoth

def convert_word_to_markdown(file_path):
    with open(file_path, "rb") as docx_file:
        result = mammoth.convert_to_markdown(docx_file)
        markdown = result.value
        # 去除Markdown中的图片
        import re
        markdown = re.sub(r'!\[.*?\]\(.*?\)', '', markdown)
        return markdown

import markdown
#from weasyprint import HTML

def convert_markdown_to_pdf(markdown_text, output_file):
    # 将Markdown转换为HTML
    html = markdown.markdown(markdown_text)
    # 使用WeasyPrint将HTML转换为PDF
    HTML(string=html).write_pdf(output_file)

"""
def read_word_document(file_path):
    doc = docx.Document(file_path)
    print(doc)
    full_text = []
    for para in doc.paragraphs:
        full_text.append(para.text)
    return '\n'.join(full_text)

from langchain_deepseek import ChatDeepSeek
from langchain_core.messages import AIMessage, HumanMessage, SystemMessage, ToolMessage
from dotenv import load_dotenv
load_dotenv()

def analyze_text_with_deepseek2(text):
    llm = ChatDeepSeek(
        model="deepseek-chat",
        temperature=0,
    )

    messages = [
        SystemMessage(content="你是一个专业的文档分析专家，能够对输入的文本进行深入分析并生成详细的分析报告。"),
        HumanMessage(content=f"请分析以下文本并生成分析报告： {text}")  # 这里是用户的输入
    ]
    response = llm.invoke(messages)
    return response.choices[0].message.content
"""

from openai import OpenAI
# 设置DeepSeek API密钥
client = OpenAI(api_key="sk-60160499be41413eb00827f4ee579294", base_url="https://api.deepseek.com")

def analyze_text_with_deepseek(text):
    try:
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": "你是一个专业的文档分析专家，能够对输入的文本进行深入分析并生成详细的分析报告。"},
                {"role": "user", "content": text},
                {"role": "user", "content": "请分析上述文本并生成分析报告"}
            ],
            stream=False,
        )
        print(response)
        return response.choices[0].message.content
    except Exception as e:
        print(f"分析过程中出现错误: {e}")
        return None

def main():
    file_path = "a.docx"  # 请替换为实际的Word文档路径
    #text = read_word_document(file_path)
    text = convert_word_to_markdown(file_path)
    #print(text)
    analysis_report = analyze_text_with_deepseek(text)
    if analysis_report:
        print(analysis_report)


if __name__ == "__main__":
    main()
