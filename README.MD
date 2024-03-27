-- build

on the folder which contains Dockerfile
D:\github\regex-hyperscan>docker build --build-arg JAR_FILE=target/*.jar -t rguner/regex-hyperscan .

-- run 

docker run rguner/regex-hyperscan


-- push

docker login --username rguner
docker push rguner/regex-hyperscan