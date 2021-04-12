from flask import Flask, jsonify, request
import json
from konlpy.tag import Okt
import os
from pprint import pprint
import nltk
import numpy as np
from tensorflow.keras import models
import urllib.request
import requests
from bs4 import BeautifulSoup

import pandas as pd
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences

app = Flask(__name__)

@app.route('/')
def index():
    value = request.args.get('test')
    result = 'hello '+value
    return result

@app.route('/movie_arr')
def movie_arr():
    url = "https://movie.naver.com/movie/running/current.nhn?order=reserve"
    soup = BeautifulSoup(urllib.request.urlopen(url).read(), "html.parser")

    tags = soup.findAll('div', attrs={'class': 'thumb'})
    movie_arr = list()
    count = 0
    for tag in tags:
        count = count+1
        if count==9:
            break
        movie_code = tag.a.get("href") # 영화 코드
        movie_title = tag.a.find('img').get("alt") # 영화 이름
        movie_img = tag.a.find('img').get("src") # 영화 이미지 src
        dict = {'movie_title':movie_title,'movie_img':movie_img,'movie_code':movie_code}
        movie_arr.append(dict)

    pprint(movie_arr)

    return jsonify(movie_arr)

@app.route('/review_arr')
def review_arr():

    movie_code = request.args.get('movie_code')

    ''' KoNLPy의 Okt는 문장의 형태소 분석과 품사 태깅을 해줌'''
    okt = Okt()

    ''' 사전 데이터를 불러오는 메소드 '''
    # def read_data(filename):
    #     with open(filename,'r',encoding='UTF-8') as f:
    #         data = [line.split('\t') for line in f.read().splitlines()]
    #         # txt 파일의 헤더 (id    document    label)는 제외하기
    #         data = data[1:]
    #     return data
    #
    # ''' 텍스트파일 변수에 저장 '''
    # train_data = read_data('ratings_train.txt')
    # test_data = read_data('ratings_test.txt')

    ''' 리뷰를 토큰화 하는 메소드 (정규화, 근어) '''
    def tokenize(doc):
        return ['/'.join(t) for t in okt.pos(doc, norm=True, stem=True)] # norm은 정규화, stem은 근어로 표시하기를 나타냄

    ''' json 파일을 변수에 저장하는 코드 '''
    with open('train_docs.json', encoding="utf-8") as f:
        train_docs = json.load(f)
    with open('test_docs.json', encoding="utf-8") as f:
        test_docs = json.load(f)

    ''' json 파일을 변수에 저장하는 코드 (json 파일이 없다면 새로 생성) '''
    # if os.path.isfile('train_docs.json'):
    #     with open('train_docs.json', encoding="utf-8") as f:
    #         train_docs = json.load(f)
    #     with open('test_docs.json', encoding="utf-8") as f:
    #         test_docs = json.load(f)
    # else:
    #     train_docs = [(tokenize(row[1]), row[2]) for row in train_data]
    #     test_docs = [(tokenize(row[1]), row[2]) for row in test_data]
    #     # JSON 파일로 저장
    #     with open('train_docs.json', 'w', encoding="utf-8") as make_file:
    #         json.dump(train_docs, make_file, ensure_ascii=False, indent="\t")
    #     with open('test_docs.json', 'w', encoding="utf-8") as make_file:
    #         json.dump(test_docs, make_file, ensure_ascii=False, indent="\t")

    ''' 이부분 정확히 이해가 안됨 '''
    tokens = [t for d in train_docs for t in d[0]]

    ''' nltk의 Text 클래스는 문서를 편리하게 탐색할 수 있는 다양한 기능을 제공 '''
    text = nltk.Text(tokens, name='NMSC')

    ''' vocab().most_common 메소드를 이용해 가장 자주 사용되는 단어를 찾아냄 '''
    selected_words = [f[0] for f in text.vocab().most_common(10000)]

    ''' 각 단어들이 사용된 횟수를 반환해주는 메소드 '''
    def term_frequency(doc):
        return [doc.count(word) for word in selected_words]

    ''' tesorflow info와 warning 메시지를 숨기는 코드 '''
    os.environ['TF_CPP_MIN_LOG_LEVEL']='2'

    ''' 학습된 모델을 불러오기 '''
    model = models.load_model('model.h5')

    review_arr = list() # 리뷰의 내용, 긍정/부정, 신뢰도를 저장할 리뷰 리스트

    ''' 학습된 데이터를 이용하여 크롤링한 리뷰 감정분석 수행하는 함수 '''
    def predict_pos_neg(review):
        if len(review.replace(" ",""))!=0:
            token = tokenize(review)
            tf = term_frequency(token)
            data = np.expand_dims(np.asarray(tf).astype('float32'), axis=0)
            score = float(model.predict(data))
            if (score > 0.5):
                dict = {'review_content': review, 'review_analysis': '긍정', 'review_analysis_percent': round(score * 100,2)}
                review_arr.append(dict)
            else:
                dict = {'review_content': review, 'review_analysis': '부정', 'review_analysis_percent': round((1 - score) * 100,2)}
                review_arr.append(dict)

    ''' 영화 코드를 인자로 입력하면 영화의 리뷰를 반환하는 함수 '''
    def getReviewResult(CODE):
        page = int(1) # 페이지 할당 ( int(1) 안하면 후에 str + int error)
        result = ''
        url = "https://movie.naver.com/movie/bi/mi/pointWriteFormList.nhn?type=after&onlyActualPointYn=N&onlySpoilerPointYn=N&order=sympathyScore&page=1&code="+str(CODE)
        soup = BeautifulSoup(urllib.request.urlopen(url).read(), "html.parser")
        total_review_count = soup.find('strong', attrs={'class':'total'}).find('em').text; # 해당 영화의 전체 리뷰 갯수
        print('00000000000000000000000000000  total_review_count : ',total_review_count)
        count = 0

        if len(total_review_count) ==1:
            print('111111111111111111111111111111')
            return 'no_review'
        elif len(total_review_count) >=3:
            print('222222222222222222222222222222')
            count = 10
        else:
            count = int(total_review_count)//10
            print('3333333333333333333333333  count : ',count)

        print('4444444444444444444444  count : ',count)
        # 반복문을 돌면서 리뷰 가져오기
        while count:
            URL = "https://movie.naver.com/movie/bi/mi/pointWriteFormList.nhn?type=after&onlyActualPointYn=N&onlySpoilerPointYn=N&order=sympathyScore&code=" + str(CODE) + "&page=" + str(page)
            resp = requests.get(URL)
            soup = BeautifulSoup(resp.content, 'html.parser')

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_0'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment0'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_1'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment1'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_2'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment2'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_3'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment3'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_4'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment4'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_5'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment5'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_6'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment6'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_7'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment7'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_8'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment8'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_9'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment9'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            count -= 1
            if not count:
                break
            page += 1


    getReviewResult(movie_code) # 크롤링하여 리뷰 가지고 와서

    pprint(review_arr)

    return jsonify(review_arr)


@app.route('/review_arr2')
def review_arr2():
    movie_code = request.args.get('movie_code')

    okt = Okt()  # KoNLPy의 Okt는 문장의 형태소 분석과 품사 태깅을 해줌

    ''' 파일에 저장된 데이터 불러오기 '''
    train_data = pd.read_table('ratings_train.txt')

    ''' null인 리뷰 제거하기 '''
    train_data = train_data.dropna(how='any')

    ''' 한글과 공백을 제외하고 모두 제거 '''
    train_data['document'] = train_data['document'].str.replace("[^ㄱ-ㅎ ㅏ-ㅣ가-힣 ]", "")

    ''' 불용어 설정 (불용어의 기준은 주관적임) '''
    stop_words = ['다', '의', '가', '이', '은', '들', '는', '.', '과', '도', '를', '으로', '자', '에', '와', '한', '하다']

    ''' 사전데이터 불러옴 '''
    with open('x_train.json', encoding="utf-8") as f:
        x_train = json.load(f)

    ''' 토큰화된 텍스트를 정수 인코딩 '''
    tokenizer = Tokenizer(num_words=35000)  # 가장 빈도가 높은 35,000개의 단어만 선택하도록 Tokenizer 객체를 만듬
    tokenizer.fit_on_texts(x_train)  # 단어 인덱스를 구축
    x_train = tokenizer.texts_to_sequences(x_train)  # 문자열을 정수 인덱스의 리스트로 변환

    ''' 리스트를 (x_train, maxlen) 크기의 2D 정수 텐서로 변환 '''
    x_train = pad_sequences(x_train, maxlen=30)

    ''' 샘플 데이터 label을 y_train에 저장'''
    y_train = np.array(train_data['label'])

    ''' tesorflow info와 warning 메시지를 숨기는 코드 '''
    os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

    ''' LSTM 알고리즘으로 학습된 모델 불러오기'''
    model = models.load_model('model2.h5')

    ''' 리뷰의 내용, 긍정/부정, 신뢰도를 저장할 리뷰 리스트 '''
    review_arr = list()

    ''' 학습된 데이터를 이용하여 크롤링한 리뷰 감정분석 수행하는 함수 '''
    def predict_pos_neg(review):
        if len(review.replace(" ", "")) != 0:
            temp_review_arr = []
            temp_review = []
            review = review.replace("[^ㄱ-ㅎ ㅏ-ㅣ가-힣 ]", "")  # 한글과 공백을 제외하고 모두 제거
            temp_review = okt.morphs(review, stem=True)  # 토큰화
            temp_review = [word for word in temp_review if not word in stop_words]  # 불용어 제거
            temp_review_arr.append(temp_review)
            temp_review_arr = tokenizer.texts_to_sequences(temp_review_arr)  # 문자열을 정수 인덱스의 리스트로 변환
            temp_review_arr = pad_sequences(temp_review_arr, maxlen=30)  # 리스트를 (x_train, maxlen) 크기의 2D 정수 텐서로 변환
            score = float(model.predict(temp_review_arr))  # 모델로 예측
            if (score > 0.5):
                dict = {'review_content': review, 'review_analysis': '긍정', 'review_analysis_percent': round(score * 100, 2)}
                review_arr.append(dict)
            else:
                dict = {'review_content': review, 'review_analysis': '부정',
                        'review_analysis_percent': round((1 - score) * 100, 2)}
                review_arr.append(dict)


    ''' 영화 코드를 인자로 입력하면 영화의 리뷰를 반환하는 함수 '''
    def getReviewResult(CODE):
        page = int(1) # 페이지 할당 ( int(1) 안하면 후에 str + int error)
        result = ''
        url = "https://movie.naver.com/movie/bi/mi/pointWriteFormList.nhn?type=after&onlyActualPointYn=N&onlySpoilerPointYn=N&order=sympathyScore&page=1&code="+str(CODE)
        soup = BeautifulSoup(urllib.request.urlopen(url).read(), "html.parser")
        total_review_count = soup.find('strong', attrs={'class':'total'}).find('em').text; # 해당 영화의 전체 리뷰 갯수

        count = 0

        if len(total_review_count) >=3:
            count = 10
        else:
            count = int(total_review_count)//10

        # 반복문을 돌면서 리뷰 가져오기
        while count:
            URL = "https://movie.naver.com/movie/bi/mi/pointWriteFormList.nhn?type=after&onlyActualPointYn=N&onlySpoilerPointYn=N&order=sympathyScore&code=" + str(CODE) + "&page=" + str(page)
            resp = requests.get(URL)
            soup = BeautifulSoup(resp.content, 'html.parser')

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_0'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment0'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_1'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment1'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_2'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment2'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_3'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment3'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_4'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment4'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_5'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment5'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_6'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment6'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_7'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment7'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_8'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment8'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            temp_result = soup.find('span', attrs={'id': '_filtered_ment_9'}).text
            if len(temp_result.strip()) > 120:
                temp_result = soup.find('span', attrs={'id': '_unfold_ment9'}).a.get('data-src')
            predict_pos_neg(temp_result.strip())

            count -= 1
            if not count:
                break
            page += 1

    getReviewResult(movie_code) # 크롤링하여 리뷰 가지고 와서

    pprint(review_arr)

    return jsonify(review_arr)

if __name__ == "__main__":
    app.run('0.0.0.0',port=5000)
