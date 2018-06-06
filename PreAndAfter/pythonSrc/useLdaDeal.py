#coding:utf-8

import warnings
warnings.filterwarnings(action='ignore', category=UserWarning, module='gensim')

import sys
import os
import gensim
from gensim import corpora,similarities,models
from collections import defaultdict
import codecs 
import json 
import jieba
import numpy

# Python2.5 初始化后会删除 sys.setdefaultencoding 这个方法，我们需要重新载入   
reload(sys)
sys.setdefaultencoding('utf-8')   	

def readFilesAndBuildCorpus():
	# 预先定义无意义词典（中文、自定义的逻辑词、无实际指代意义的词）
	unusefulWords = {}.fromkeys(['的', '对于', '对', '为', '被', '使', '致使', '可以', '应当', '或', '或者', '同时', '但是', '而', '其他', '以下', '下列', '如下', '之一', '有', '情节', '特别', '严重', '情节严重', '情节特别严重', '较轻', '较大', '较小', '较', '轻', '重', '巨大', '数额', '数额巨大', '造成', '后果', '情形', '影响', '他人', '国家', '其', '犯', '罪', '本法', '所', '称', '所称', '在', '中', '第', '款', '第一款', '第二款', '第三款', '，', '、', '；', '。'])
	# print json.dumps(unusefulWords, encoding='UTF-8', ensure_ascii=False)

	# 读取文件内容
	documents = []
	file = codecs.open("../txtSrc/results-try.txt",'r',"utf-8")
	for line in file:
		if len(line)==0 or line=='\n':
			continue;
		antc = line.rsplit('\t', 2)[1]
		anItems = jieba.cut(antc, cut_all=False)
		seg_list = []
		for anItem in anItems:
			anItem = anItem.encode('UTF-8')
			if anItem not in unusefulWords:
				seg_list.append(anItem)
		documents.append(seg_list)
	file.close()

	'''
	texts = [[word for word in document if word not in unusefulWords] 
			for document in documents] #因为已经把前件进行了切分，所以这里不需要再次split()
	texts = [[word for word in document.split() if word not in unusefulWords] for document in documents]'''
	texts = [document for document in documents]
	# print json.dumps(texts, encoding='UTF-8', ensure_ascii=False)

	# 去掉仅出现一次的单词
	'''
	frequency = defaultdict(int)
	for text in texts:
		for token in text:
			frequency[token] += 1
	texts = [[token for token in text if frequency[token] > 1]
			 for text in texts]
	texts=documents;
	'''

	# 生成并保存词典
	dictionary = corpora.Dictionary(texts) 
	dictionary.save('mydict.dic')
	# 生成并保存语料库
	corpus = [dictionary.doc2bow(list(text)) for text in texts]
	corpora.MmCorpus.serialize('corpus.mm', corpus)

def trainModels():
	# 加载语料库
	if os.path.exists('mydict.dic') and os.path.exists('corpus.mm'):
		dictionary = corpora.Dictionary.load('mydict.dic')
		corpus = corpora.MmCorpus('corpus.mm')
		# print 'used files generated from string2vector'
	else:
		print 'please run string2vector firstly'

	#创建一个model  
	tfidf = models.TfidfModel(corpus=corpus)
	tfidf.save('model.tfidf')
	#使用创建好的model生成一个对应的向量
	vector = tfidf[corpus[0]]
	# print(vector)
	#序列化
	tfidf_corpus = tfidf[corpus]
	corpora.MmCorpus.serialize('tfidf_corpus.mm', tfidf_corpus)

	#lsi
	'''
	lsi = models.LsiModel(corpus = tfidf_corpus,id2word=dictionary,num_topics=20)
	lsi_corpus = lsi[tfidf_corpus]
	lsi.save('model.lsi')
	corpora.MmCorpus.serialize('lsi_corpus.mm', lsi_corpus)
	print 'LSI Topics:'
	lsitopics=lsi.print_topics(10)
	print json.dumps(lsitopics, encoding='UTF-8', ensure_ascii=False)
	'''

	#lda
	'''
	lda = models.LdaModel(corpus = tfidf_corpus,id2word=dictionary,num_topics=20)
	lda_corpus = lda[tfidf_corpus]
	lda.save('model.lda')
	corpora.MmCorpus.serialize('lda_corpus.mm', lda_corpus)
	print('LDA Topics:')
	ldatopics=lda.print_topics(20)
	print(json.dumps(ldatopics, encoding='UTF-8', ensure_ascii=False))
	'''

def readFilesAndDealWithEachAntecedent():	
	# 读取文件内容
	documents = []
	file = codecs.open("../txtSrc/results-try.txt",'r',"utf-8")
	lineNo = 0;
	for line in file:
		if len(line)==0 or line=='\n':
			continue;
		antc = line.rsplit('\t', 2)[1]
		similarSentences = getSimilarSentences(antc)
		similarPercent = 0
		accuracy = 0
		for index in range(len(similarSentences)):
			valuePair = str(similarSentences[index])
			similarSentenceNo = valuePair.split(',')[0]
			if lineNo == similarSentenceNo:
				print(index)
				similarPercent = float(valuePair.split(',')[1])
				accuracy = ( (len(similarSentences) - index) * 1.0 / len(similarSentences))
				break
		print("实验句："+antc+"，相关法条匹配准确度："+str(accuracy))
	file.close()	
	
def getSimilarSentences(sentence):
	# 首先加载语料库
	if os.path.exists('lsi_corpus.mm') and os.path.exists('mydict.dic'):
		dictionary = corpora.Dictionary.load('mydict.dic')  
		corpus = corpora.MmCorpus('lsi_corpus.mm')  
		model = models.LsiModel.load('model.lsi')  
		# print('used files generated from topics')
	else:  
		print('please run topics firstly')  
		
	index = similarities.MatrixSimilarity(corpus)  
	index.save('lsi_similarity.sim')  
	# 输出语料库中与测试用例最相近的句子
	bow_vec = dictionary.doc2bow(jieba.lcut(sentence))
	lsi_vec = model[bow_vec]
	sims = index[lsi_vec]
	sims = sorted(enumerate(sims), key=lambda item: -item[1])
	return sims[:100] #返回前100个与测试用例最相近的句子

def get_dict(sentence):
	train = []
	line = list(jieba.cut(sentence))
	train.append([word for word in line])

	dictionary = corpora.Dictionary(train)
	return dictionary,train

	
# 用LSI计算两个句子的相似度
def useLsiComputeSimilarityOfTwoSentences(s1,s2):
	
	# 预先定义无意义词典（中文、自定义的逻辑词、无实际指代意义的词）
	unusefulWords = {}.fromkeys(['的', '对于', '对', '为', '被', '使', '致使', '可以', '应当', '或', '或者', '同时', '但是', '而', '其他', '以下', '下列', '如下', '之一', '有', '情节', '特别', '严重', '情节严重', '情节特别严重', '较轻', '较大', '较小', '较', '轻', '重', '巨大', '数额', '数额巨大', '造成', '后果', '情形', '影响', '他人', '国家', '其', '犯', '罪', '本法', '所', '称', '所称', '在', '中', '第', '款', '第一款', '第二款', '第三款', '，', '、', '；', '。'])
	
	lsi = models.lsimodel.LsiModel.load('model.lsi')
	dictionary=get_dict(s1)[0]
	
	test_doc1 = list(jieba.cut(s1, cut_all=False))  # 句子1进行精确模式分词
	test_doc1 = [word for word in test_doc1 if word not in unusefulWords] # 去除句子1中的无用词
	doc_bow1 = dictionary.doc2bow(test_doc1)  # 文档转换成bow
	doc_lsi1 = lsi[doc_bow1]  # 得到句子1的主题分布
	# print(doc_lsi1)
	list_doc1 = [i[1] for i in doc_lsi1]
	# print('list_doc1',list_doc1)

	test_doc2 = list(jieba.cut(s2, cut_all=False))  # 句子2进行精确模式分词
	test_doc2 = [word for word in test_doc2 if word not in unusefulWords] # 去除句子2中的无用词
	doc_bow2 = dictionary.doc2bow(test_doc2)  # 文档转换成bow
	doc_lsi2 = lsi[doc_bow2]  # 得到句子2的主题分布
	# print(doc_lsi2)
	list_doc2 = [i[1] for i in doc_lsi2]
	# print('list_doc2',list_doc2)

	try:
		sim = numpy.dot(list_doc1, list_doc2) / (numpy.linalg.norm(list_doc1) * numpy.linalg.norm(list_doc2))
	except ValueError:
		sim=0
	print("LSI:"+str(sim)) #得到文档之间的相似度，越大表示越相近

	
# 用LDA计算两个句子的相似度
def useLdaComputeSimilarityOfTwoSentences(s1,s2):
	
	# 预先定义无意义词典（中文、自定义的逻辑词、无实际指代意义的词）
	unusefulWords = {}.fromkeys(['的', '对于', '对', '为', '被', '使', '致使', '可以', '应当', '或', '或者', '同时', '但是', '而', '其他', '以下', '下列', '如下', '之一', '有', '情节', '特别', '严重', '情节严重', '情节特别严重', '较轻', '较大', '较小', '较', '轻', '重', '巨大', '数额', '数额巨大', '造成', '后果', '情形', '影响', '他人', '国家', '其', '犯', '罪', '本法', '所', '称', '所称', '在', '中', '第', '款', '第一款', '第二款', '第三款', '，', '、', '；', '。'])
	
	lda = models.ldamodel.LdaModel.load('model.lda')
	dictionary=get_dict(s1)[0]
	
	test_doc1 = list(jieba.cut(s1, cut_all=False))  # 句子1进行精确模式分词
	test_doc1 = [word for word in test_doc1 if word not in unusefulWords] # 去除句子1中的无用词
	doc_bow1 = dictionary.doc2bow(test_doc1)  # 文档转换成bow
	doc_lda1 = lda[doc_bow1]  # 得到句子1的主题分布
	# print(doc_lda1)
	list_doc1 = [i[1] for i in doc_lda1]
	# print('list_doc1',list_doc1)

	test_doc2 = list(jieba.cut(s2, cut_all=False))  # 句子2进行精确模式分词
	test_doc2 = [word for word in test_doc2 if word not in unusefulWords] # 去除句子2中的无用词
	doc_bow2 = dictionary.doc2bow(test_doc2)  # 文档转换成bow
	doc_lda2 = lda[doc_bow2]  # 得到句子2的主题分布
	# print(doc_lda2)
	list_doc2 = [i[1] for i in doc_lda2]
	# print('list_doc2',list_doc2)

	try:
		sim = numpy.dot(list_doc1, list_doc2) / (numpy.linalg.norm(list_doc1) * numpy.linalg.norm(list_doc2))
	except ValueError:
		sim=0
	print("LDA:"+str(sim)) #得到文档之间的相似度，越大表示越相近

	
# 如果是模拟控制台运行的话，需要加上下面这行代码
# gettSimilarSentences(sys.argv[1])
# readFilesAndDealWithEachAntecedent()
useLsiComputeSimilarityOfTwoSentences("有期徒刑一年两个月","三年以下有期徒刑")
useLdaComputeSimilarityOfTwoSentences("有期徒刑一年两个月","三年以下有期徒刑")