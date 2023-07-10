package pinponballmain;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Game extends JFrame { 

	private static final int WIDTH = 600; // 視窗寬
	private static final int HEIGHT = 800;// 視窗高

	private JPanel gamePanel; // 遊戲面板屬性
	private Timer timer; // 時間屬性
	private int ballX, ballY; // 球的x,y座標
	private int paddleX;
	private int paddleX_RED;
	private int ballSpeedX, ballSpeedY; // 球速
	private int score_bule;// 分數
	private int score_red;// 分數
	private boolean isGameRunning;
	private String Player;
	private int Score;
	private int countdown = 100000; // 計時器60秒

	private static final int PADDLE_WIDTH_BULE = 100;// 藍方寬度 //強化玩家
	private static final int PADDLE_WIDTH_RED = 100;// 紅方寬度 //強化玩家
	private static final int PADDLE_HEIGHT_BLUE = 10;// 藍方高度 //無意義
	private static final int PADDLE_HEIGHT_RED = 10;// 紅方高度//無意義
	private static final int BALL_SIZE = 20;
	private static final int PADDLE_Y_RED = HEIGHT - PADDLE_HEIGHT_RED - 790;// 紅方高低度
	private static final int PADDLE_Y = HEIGHT - PADDLE_HEIGHT_BLUE - 40;// 藍方高低度
	private static final int BALL_INITIAL_X = WIDTH / 2 - BALL_SIZE / 2; // 初始化球的X,讓球再玩家中間
	private static int BALL_INITIAL_Y = 720;// 初始化球的Y,讓球再玩家中間

	private String playerName_blue;
	private String playerName_red;

	public Game() {

		rank();// 調用排行榜sql
		setTitle("足球小遊戲"); // 視窗標題
		setSize(WIDTH, HEIGHT); // 視窗大小(帶入變數)
		setResizable(false); // 固定視窗大小 使用戶無法調整大小

		setDefaultCloseOperation(EXIT_ON_CLOSE);// 設定視窗案右上X會關閉視窗並停止程序,反之沒有設定雖可以關閉視窗但程序能繼續執行

		gamePanel = new JPanel() {

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				draw(g);
			}

		};

		gamePanel.setFocusable(true); // 鍵盤移動開關
		// gamePanel.setBackground(Color.BLACK);//視窗背景
		showInputDialog();

		gamePanel.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyPress(e);
			}
		});

		add(gamePanel);
		initializeGame();

		setVisible(true);
	}

	private void showInputDialog() {

		playerName_blue = JOptionPane.showInputDialog(this, "請輸入您的名字：", "藍方玩家", JOptionPane.PLAIN_MESSAGE);
		playerName_red = JOptionPane.showInputDialog(this, "請輸入您的名字：", "紅方玩家", JOptionPane.PLAIN_MESSAGE);
		if (playerName_blue == null || playerName_blue.trim().isEmpty()) {
			if (playerName_red == null || playerName_red.trim().isEmpty()) {
				int choice = JOptionPane.showConfirmDialog(this, "確定要離開遊戲吗？", "離開遊戲", JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					System.exit(0);
				} else {
					showInputDialog();
				}
			}
		}
	}

	private void initializeGame() {
		ballX = BALL_INITIAL_X; // 使求再藍方X中間
		ballY = BALL_INITIAL_Y; // 使求在藍方Y中間
		paddleX = WIDTH / 2 - PADDLE_WIDTH_BULE / 2; // 視窗寬度/2-藍方寬度=使藍方在正中間位子
		paddleX_RED = WIDTH / 2 - PADDLE_WIDTH_RED / 2; // 視窗寬度/2-紅方寬度=使紅方在正中間位子
		ballSpeedX = 6; // 球速X
		ballSpeedY = -6;// 球速Y

		isGameRunning = true; // 遊戲是否進行中初始化(即為剛開始遊戲=遊戲進行中 默認為true)

		gamePanel.requestFocusInWindow();
		startGameLoop();
	}

	private void startGameLoop() {
		timer = new Timer(20, new ActionListener() { // 球的動作時間間隔
			public void actionPerformed(ActionEvent e) {
				updateGame();// 調用//使球隨時更新座標(移動)方法
				gamePanel.repaint();
			}
		});
		timer.start();// 執行上面time
	}

	private void updateGame() { // 使球隨時更新座標(移動)
		if (countdown > 0) {
			countdown=countdown-40;
			if (countdown == 0) {
				gameOver();
			}
		}

		ballX += ballSpeedX; // 藍方輸了重新發球
		ballY += ballSpeedY;
//    	if() {
//        ballX += ballSpeedX;  //藍方輸了重新發球
//        ballY += ballSpeedY; ////藍方輸了重新發球
//    	}else if() {
//    	ballX -= ballSpeedX;  //藍方輸了重新發球
//        ballY -= ballSpeedY; ////藍方輸了重新發球	
//    	}
		if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) {
			ballSpeedX = -ballSpeedX;
		}

		if (ballY >= HEIGHT - BALL_SIZE - BALL_SIZE - BALL_SIZE - PADDLE_HEIGHT_BLUE) { // 藍方撞球判斷
			// 如果球Y >= 視窗高度 - 球體 - 藍方高度
			if (ballX >= paddleX && ballX <= paddleX + PADDLE_WIDTH_BULE) {
				ballSpeedY = -ballSpeedY; // 彈回
				ballSpeedY--;

				System.out.println("藍色已攔截");
			} else {

				System.out.println("紅色得分!!");
				score_red++;
				changeball01();
				gameOver();

			}
		}

		if (ballY <= PADDLE_HEIGHT_RED) {
			// 球Y <= 紅方高低度
			// 如果球Y >= 視窗高度 - 球體 - 紅方高度
			if (ballX >= paddleX_RED && ballX <= paddleX_RED + PADDLE_WIDTH_RED) {
				ballSpeedY = -ballSpeedY;
				ballSpeedY++;
				System.out.println("紅色已攔截");

			} else {
				System.out.println("藍色得分!!");
				score_bule++;
				changeball();
				gameOver();

			}
		}

	}

	private void draw(Graphics g) {
		try {
			Image backgroundImage = ImageIO.read(getClass().getResource("/02.png"));
			Image ballImage = ImageIO.read(getClass().getResource("/football.png"));
			String imagePath = "新的圖片路徑.jpg"; // 替換為您想要的新圖片路徑

			// 使用ImageIO讀取圖片

			g.drawImage(backgroundImage, -4, -20, getWidth(), getHeight(), null);
			g.drawImage(ballImage, ballX, ballY, BALL_SIZE, BALL_SIZE, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Font font = new Font(null, Font.BOLD, 20);//創建字體物件並設定字體與大小
		Font font01 = new Font(null, Font.BOLD, 10);//創建字體物件並設定字體與大小
//		g.setColor(Color.WHITE);
//		g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);// 球體方
		

		g.setColor(Color.BLUE);
		g.fillRect(paddleX, PADDLE_Y, PADDLE_WIDTH_BULE, PADDLE_HEIGHT_BLUE);// 玩家藍方

		g.setColor(Color.RED);
		g.fillRect(paddleX_RED, PADDLE_Y_RED, PADDLE_WIDTH_RED, PADDLE_HEIGHT_RED);// 玩家紅方
		
		g.setFont(font);//設定字體
		g.setColor(Color.blue);// 分數顯示
		g.drawString("藍方分數: " + score_bule, 40, 400);
		g.setColor(Color.blue);
		g.drawString("藍方玩家名稱: " + playerName_blue, 40, 420);
		g.setColor(Color.red);
		g.drawString("紅方分數: " + score_red, 40, 365);
		g.setColor(Color.red);
		g.drawString("紅方玩家名稱: " + playerName_red, 40, 345);
		g.setColor(Color.black);
		g.drawString("最高分數玩家:" + Player, 400, 365);
		g.drawString("最高分數:" + Score, 400, 400);
		g.setColor(Color.black);
		g.setFont(font);//設定字體
		g.drawString("剩餘時間: " + countdown/1000+"秒", 100, 100);
		
		repaint();
	}

	private void handleKeyPress(KeyEvent e) {
		int key = e.getKeyCode();// 藍方控制
		int key1 = e.getKeyCode();// 紅方控制

		if (key == KeyEvent.VK_LEFT) {
			paddleX -= 20;
			if (paddleX < 0) {
				paddleX = 0;
			}
		} else if (key == KeyEvent.VK_RIGHT) {
			paddleX += 20;
			if (paddleX > WIDTH - PADDLE_WIDTH_BULE) { // 判斷如果碰到邊界
				paddleX = WIDTH - PADDLE_WIDTH_BULE; // 就讓他停在邊界
			}
		}

		if (key1 == KeyEvent.VK_A) { // 如果案A
			paddleX_RED -= 20;// X-20參數(往左走)
			if (paddleX_RED < 0) {
				paddleX_RED = 0;
			}
		} else if (key1 == KeyEvent.VK_D) { // 如果案D
			paddleX_RED += 20; // X+20參數(往右走)
			if (paddleX_RED > WIDTH - PADDLE_WIDTH_BULE) { // 判斷如果碰到邊界
				paddleX_RED = WIDTH - PADDLE_WIDTH_BULE; // 就讓他停在邊界
			}
		}
	}

	private void gameOver() {

		initializeGame();// 遊戲初始化
		timer.stop();// 停止timer

		if (countdown == 0) { // 遊戲時間到

			if (score_bule > score_red) {
				// int choice = JOptionPane.showConfirmDialog(this, "遊戲結束藍方勝利 ", "Game Over",
				// JOptionPane.YES_NO_OPTION);
				String[] options = { "重新開始", "離開遊戲" };
				int choice = JOptionPane.showOptionDialog(this, "遊戲結束藍方勝利 ", "獲勝者訊息", 0, 3, null, options, options[0]);
				if (choice == 0) { // 如果按下數組[0] 即為重新開始
					balljdbc01();
					rank();
					showInputDialog();
					countdown = 10000;
					score_bule = 0;
					score_red = 0;
					initializeGame();
					timer.stop();

				} else if (choice == 1) {// 如果按下數組[1] 即為離開遊戲
					balljdbc01();
					System.exit(0);
				}
			} else if (score_red > score_bule) {

				// int choice = JOptionPane.showConfirmDialog(this, "遊戲結束紅方勝利 ", "Game Over",
				// JOptionPane.YES_NO_OPTION);
				String[] options = { "重新開始", "離開遊戲" };
				int choice = JOptionPane.showOptionDialog(this, "遊戲結束紅方勝利 ", "獲勝者訊息", 0, 3, null, options, options[0]);
				if (choice == 0) {
					balljdbc01();
					rank();
					showInputDialog();
					countdown = 10000;
					score_bule = 0;
					score_red = 0;
					initializeGame();
					timer.stop();

				} else if (choice == 1) {
					balljdbc01();
					System.exit(0);

				}

			}
			if (countdown == 0) {
				String[] options = { "重新開始", "離開遊戲" };
				int choice = JOptionPane.showOptionDialog(this, "雙手平手!! ", "獲勝者訊息", 0, 3, null, options, options[0]);
				if (choice == 0) {
					balljdbc01();
					rank();
					showInputDialog();
					countdown = 10000;
					score_bule = 0;
					score_red = 0;
					initializeGame();
					timer.stop();

				} else {
					balljdbc01();
					System.exit(0);
				}

			}

		}
	}

	public void changeball() { // 改變球的初始=換藍色發球(代表紅方已射門得分)
		BALL_INITIAL_Y = 10;//
		;

	}

	public void changeball01() { // 改變球的初始=換紅色發球(代表藍方已射門得分)
		BALL_INITIAL_Y = 720;//
		

	}

	public void balljdbc01() { // 連接資料庫並判斷誰贏 贏的人插入數據

		if (score_bule > score_red) {
			try {
				Properties prop = new Properties();
				prop.put("user", "root");
				prop.put("password", "root");
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/iii", prop);

				String sql = "INSERT INTO ballgame(name, score) VALUES (?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);

				stmt.setString(1, playerName_blue);
				stmt.setInt(2, score_bule);

				try {
					stmt.executeUpdate();
				} catch (Exception e) {
					System.out.println(e);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		} else if (score_red > score_bule) {
			try {
				Properties prop = new Properties();
				prop.put("user", "root");
				prop.put("password", "root");
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/iii", prop);

				String sql = "INSERT INTO ballgame(name, score) VALUES (?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);

				stmt.setString(1, playerName_red);
				stmt.setInt(2, score_red);

				try {
					stmt.executeUpdate();
				} catch (Exception e) {
					System.out.println(e);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		} else if (score_red == score_bule) {
			try {
				Properties prop = new Properties();
				prop.put("user", "root");
				prop.put("password", "root");
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/iii", prop);

				String sql = "INSERT INTO ballgame(name, score) VALUES (?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				
				stmt.setString(1, playerName_blue);
				stmt.setInt(2, score_bule);
			

				try {
					stmt.executeUpdate();
				} catch (Exception e) {
					System.out.println(e);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}else if(score_bule == score_red) {
			try {
				Properties prop = new Properties();
				prop.put("user", "root");
				prop.put("password", "root");
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/iii", prop);

				String sql = "INSERT INTO ballgame(name, score) VALUES (?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				
				stmt.setString(1, playerName_red);
				stmt.setInt(2, score_red);
			

				try {
					stmt.executeUpdate();
				} catch (Exception e) {
					System.out.println(e);
				}
			} catch (Exception e) {
				System.out.println(e);
			}

		}
	}

	// -------------------------
	public void rank() { // 排行榜

		try {
			Properties prop = new Properties();
			prop.put("user", "root");
			prop.put("password", "root");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/iii", prop);

			String sql = "SELECT * FROM ballgame ORDER BY score DESC LIMIT 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Player = rs.getString("name");
				Score = rs.getInt("score");
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		new Game();
		// SwingUtilities.invokeLater(Game::new);

	}
}