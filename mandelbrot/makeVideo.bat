call ffmpeg -framerate 15 -i img%%%%03d.png -c:v libx264 -pix_fmt yuv420p video.mp4