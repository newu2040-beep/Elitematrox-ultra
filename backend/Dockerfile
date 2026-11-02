FROM node:20-bookworm-slim

# Install maintained FFmpeg and FFprobe with libx264, libx265, libsvtav1, and filter support
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install --omit=dev

COPY server.js ./

ENV PORT=8080
EXPOSE 8080

USER node
CMD ["node", "server.js"]
