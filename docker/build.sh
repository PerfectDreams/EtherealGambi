image_name="etherealgambi-base"

docker build . --file Dockerfile --tag $image_name
docker save $image_name -o image.tar