# learnerTweets-service

Build a Probabilistic Topic Model from a corpus containing tweets

To test this service follow these simple steps:

1.- Clone the project:
```sh
$ git clone https://github.com/librairy/learnerTweets-service.git
```

2.- Go into the project folder:
```sh
$ cd learnerTweets-service
```

3.- Run the service (docker-compose is required):
```sh
$ docker-compose up -d
```

4.- Check the service is up and running
```sh
docker-compose logs -f
```

5.- Request a new model by using the 'build' command passing a file path as argument:
```sh
./build 1k-tweets.csv
```

6.- Finally, check again the service is working on that:
```sh
docker-compose logs -f
```


Then, a new folder is created with the name of file as prefix:
```sh
cd 1k-tweets.csv-model-*
```

And there, the following files will be available:
1.- **model.json**: for inference purpose
2.- **phi.txt**: distribution of words per topic
3.- **theta**: distribution of topics per tweet
4.- **topics**: summary of topics and words
5.- **vocabulary**: all the words used in the model

Now, you can infer topic distributions for new tweets by using the existing model:
```sh
./infer 1k-tweets.csv 1k-tweets.csv-model-*
```

