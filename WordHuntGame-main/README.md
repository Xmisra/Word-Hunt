Here’s the README in a single Markdown code block so you can directly paste it into your GitHub README file:

# Word Matching Game

A real-time multiplayer word game where players compete to find valid words within a grid of letters. The game is built using **Java Swing, Multithreading, Networking, and JDBC** for database integration with **MySQL**.

## 🚀 Features
- **Multiplayer gameplay** using Java Socket Programming
- **Real-time timer and score tracking**
- **MySQL integration** for player authentication and leaderboard storage
- **Interactive GUI** using Java Swing
- **Validates words** using a predefined dictionary file (`dictionary.txt`)

## 📂 Project Structure

word-matching-game/
│── src/
│   ├── Client.java
│   ├── Server.java
│   ├── DatabaseHandler.java
│   ├── dictionary.txt
│
│── lib/
│   ├── mysql-connector-j-9.2.0.jar
│
│── README.md

## 🛠️ Requirements
- **Java 11** or later
- **MySQL Server** installed and running
- **MySQL Connector JAR** (included in `lib/`)

## 📥 Setup Instructions
1. **Clone the repository** or download the source code:
   ```sh
   git clone https://github.com/yourusername/word-matching-game.git
   cd word-matching-game

	2.	Install MySQL and create a database named wordgame:

mysql -u root -p
CREATE DATABASE wordgame;
USE wordgame;

(Tables will be automatically created by DatabaseHandler.java.)

	3.	Update the database credentials in DatabaseHandler.java.
	4.	Ensure mysql-connector-j-9.2.0.jar is included in the classpath.

▶️ Compiling and Running

On Windows:

# Open Command Prompt in the project directory
javac -cp "lib/;src/" -d bin src/*.java
java -cp "lib/*;bin/" Server  # Start the server
java -cp "lib/*;bin/" Client  # Start a client (in a new terminal)

On macOS/Linux:

# Open Terminal in the project directory
javac -cp "lib/:src/" -d bin src/*.java
java -cp "lib/*:bin/" Server  # Start the server
java -cp "lib/*:bin/" Client  # Start a client (in a new terminal)

🏗️ Database Setup
	1.	Start MySQL Server
	2.	Run the following commands:

mysql -u root -p
CREATE DATABASE wordgame;
USE wordgame;

(Tables are automatically created by DatabaseHandler.java.)

🎮 How to Play
	1.	Enter your name to join the game.
	2.	Find words using the given letter grid.
	3.	Submit words to earn points.
	4.	The player with the highest score at the end wins!

🔧 Troubleshooting
	•	ClassNotFoundException for MySQL Driver?
	•	Ensure mysql-connector-j-9.2.0.jar is in the lib/ folder and included in the classpath.
	•	MySQL not connecting?
	•	Verify the MySQL server is running and the credentials in DatabaseHandler.java are correct.
	•	Dictionary file missing?
	•	Ensure dictionary.txt is in the src/ folder.

📜 License

This project is for educational purposes and is open-source.

👨‍💻 Developed By

Name	Roll Number
Soumik Misra	524110037
Soumyajit Saha	524110038
Sourav Mondal	524110048
Niket Sarkar	524110064

