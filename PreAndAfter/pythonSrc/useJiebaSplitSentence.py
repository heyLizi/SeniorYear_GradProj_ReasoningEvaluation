#coding:utf-8

import sys
import json
import jieba
import jieba.posseg

# Python2.5 初始化后会删除 sys.setdefaultencoding 这个方法，我们需要重新载入   
reload(sys)
sys.setdefaultencoding('utf-8')
	
# 分离句子，得到句子分词结果
def splitSentence(sentence):
	
	# 这里用lcutXX方法返回list对象，否则用cutXX方法返回generator对象，无法被调用它的java程序捕获输出
	# seg_list = jieba.lcut_for_search(sentence)# 搜索引擎模式，
	seg_list = jieba.lcut(sentence, cut_all=False) # 全模式
	
	# 以下几行代码是通过Jython获取返回结果时使用的，不能通过控制台调用
	# str = ",".join(seg_list)	# 这句话在命令行中直接调用会报错，process.waitFor()方法
	# return str # 返回结果给jython
	
	# 以下几行代码是通过控制台调用使用的，不能通过Jython获取返回结果
	# 转换为json格式，否则无法正常输出中文列表	
	seg_list_json = json.dumps(seg_list, encoding='UTF-8', ensure_ascii=False)
	print seg_list_json # 输出结果到控制台，被Java捕获

# 分离句子，对句子中的单词进行词性标注
def markWordProperty(sentence):	
	
	seg = jieba.posseg.cut(sentence)  

	list = []
	for i in seg:
		list.append((i.word, i.flag))  # 把单词和词性加入列表中
	
	# 转换为json格式，否则无法正常输出中文列表	
	list_json = json.dumps(list, encoding='UTF-8', ensure_ascii=False) 
	print(list_json)

# 如果是模拟控制台运行的话，需要加上下面这行代码	
markWordProperty(sys.argv[1])