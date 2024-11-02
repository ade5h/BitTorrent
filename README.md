## Steps to run this BitTorrent client on your local machine

- Make sure you've got Java 17 and maven installed on your machine
- Clone this repo and cd into it:
```declarative
git clone https://github.com/ade5h/BitTorrent.git
cd BitTorrent
```
- Compile this project using maven. This creates a jar file in the target directory
```declarative
mvn clean install
```
- Run the application 
```declarative
java -jar target/bittorrent.jar <nameOfTheTorrentFile> <downloadPath>
```
- For example, you can test it using the sample.torrent file provided
```declarative
java -jar target/bittorrent.jar sample.torrent output/file.txt
```