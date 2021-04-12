from konlpy.tag import Okt
import pandas as pd
from pprint import pprint
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
import numpy as np
from datetime import datetime
import os

######################
# 데이터 전처리
######################
okt = Okt() # KoNLPy의 Okt는 문장의 형태소 분석과 품사 태깅을 해줌

''' 파일에 저장된 데이터 불러오기 '''
train_data = pd.read_table('ratings_train.txt')

''' null인 리뷰 제거하기 '''
train_data = train_data.dropna(how='any')

''' 한글과 공백을 제외하고 모두 제거 '''
train_data['document'] = train_data['document'].str.replace("[^ㄱ-ㅎ ㅏ-ㅣ가-힣 ]","")

''' 불용어 설정 (불용어의 기준은 주관적임) '''
stop_words = ['다','의','가','이','은','들','는','.','걍','과','도','를','으로','자','에','와','한','하다']

''' KoNLPy의 Okt로 텍스트 토큰화 (stem=True : 일정 수준의 정규화를 수행해줌) '''
x_train = []
for sentence in train_data['document']:
    temp_x = []
    temp_x = okt.morphs(sentence,stem=True) # 토근화
    temp_x = [word for word in temp_x if not word in stop_words] # 불용어 제거
    x_train.append(temp_x)

if os.path.isfile('test.json'):
    with open('test.json', encoding="utf-8") as f:
        train_docs = json.load(f)
else:
    train_docs = [(tokenize(row[1]), row[2]) for row in x_train]
    # JSON 파일로 저장
    with open('test.json', 'w', encoding="utf-8") as make_file:
        json.dump(train_docs, make_file, ensure_ascii=False, indent="\t")


''' 토큰화된 텍스트를 정수 인코딩 '''
tokenizer = Tokenizer(num_words=35000) # 가장 빈도가 높은 35,000개의 단어만 선택하도록 Tokenizer 객체를 만듬
tokenizer.fit_on_texts(x_train) # 단어 인덱스를 구축
x_train = tokenizer.texts_to_sequences(x_train) # 문자열을 정수 인덱스의 리스트로 변환

''' 리스트를 (x_train, maxlen) 크기의 2D 정수 텐서로 변환 '''
x_train = pad_sequences(x_train, maxlen=30) # 이 과정이 왜 필요한지 모르겠음

''' 샘플 데이터 label을 y_train에 저장'''
y_train=np.array(train_data['label'])

############################################
# LSTM으로 네이버 영화 리뷰 감성 분류하기
############################################

from tensorflow.keras.layers import Embedding, Dense, LSTM
from tensorflow.keras.models import Sequential

os.environ['TF_CPP_MIN_LOG_LEVEL']='2' # tesorflow info와 warning 메시지를 숨기는 코드

''' 모델 생성 '''
# model = Sequential()
# model.add(Embedding(35000, 100))
# model.add(LSTM(128))
# model.add(Dense(1, activation='sigmoid'))
# print(now, '  >>  모델 생성')

''' 모델 컴파일 '''
# model.compile(optimizer='rmsprop', loss='binary_crossentropy', metrics=['acc'])
# print(now, '  >>  모델 컴파일')

''' 모델 훈련 '''
# history = model.fit(x_train, y_train, epochs=4, batch_size=60, validation_split=0.2)
# print(now, '  >>  모델 훈련')

''' 모델 저장 '''
# from keras.models import load_model
# model.save('model2.h5')
# print(now, '  >>  모델 저장')

''' 모델 불러오기'''
from tensorflow.keras import models
model = models.load_model('model2.h5')

''' 학습된 데이터를 이용하여 크롤링한 리뷰 감정분석 수행하는 함수 '''
def predict_pos_neg(review):
    if len(review.replace(" ",""))!=0:
        review_arr = []
        temp_review = []
        review = review.replace("[^ㄱ-ㅎ ㅏ-ㅣ가-힣 ]","") # 한글과 공백을 제외하고 모두 제거
        temp_review = okt.morphs(review, stem=True) # 토큰화
        temp_review = [word for word in temp_review if not word in stop_words]  # 불용어 제거
        review_arr.append(temp_review)
        review_arr = tokenizer.texts_to_sequences(review_arr) # 문자열을 정수 인덱스의 리스트로 변환
        review_arr = pad_sequences(review_arr, maxlen=30) # 리스트를 (x_train, maxlen) 크기의 2D 정수 텐서로 변환
        score = float(model.predict(review_arr)) # 모델로 예측
        if (score > 0.5):
            dict = {'review_content': review, 'review_analysis': '긍정', 'review_analysis_percent': round(score * 100,2)}
            print('review_content : '+review+', review_analysis :  긍정, review_analysis_percent : ', round(score * 100,2))
        else:
            dict = {'review_content': review, 'review_analysis': '부정', 'review_analysis_percent': round((1 - score) * 100,2)}
            print('review_content : ' + review + ', review_analysis :  부정, review_analysis_percent : ', round(score * 100, 2))


predict_pos_neg("올해 최고의 영화! 세 번 넘게 봐도 질리지가 않네요.")
predict_pos_neg("배경 음악이 영화의 분위기랑 너무 안 맞았습니다. 몰입에 방해가 됩니다.")
predict_pos_neg("주연 배우가 신인인데 연기를 진짜 잘 하네요. 몰입감 ㅎㄷㄷ")
predict_pos_neg("믿고 보는 감독이지만 이번에는 아니네요")
predict_pos_neg("주연배우 때문에 봤어요")
