import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener
{
    int boardWidth = 360;
    int boardHeight = 640;

    // images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // 🔊 SOUND
    Clip bgMusic;
    Clip gameOverSound;
    boolean soundPlayed = false;

    // Bird
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img){
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;

    double score = 0;
    boolean gameOver = false;

    // 🔊 LOAD SOUND METHOD
    public Clip loadSound(String path) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(getClass().getResource(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    FlappyBird()
    {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // 🔊 LOAD SOUNDS (YOUR FILE NAMES)
        bgMusic = loadSound("/mar-gya-madarchod.wav");
        gameOverSound = loadSound("/faahhhhhh.wav");

        // start background loop
        if (bgMusic != null) {
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // pipes timer
        placePipesTimer = new Timer(1500, e -> placePipes());
        placePipesTimer.start();

        // game loop
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    public void placePipes()
    {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g)
    {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for(int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));

        if(gameOver) {
            g.drawString("Game Over: " + (int)score, 10, 35);
        } else {
            g.drawString(String.valueOf((int)score), 10, 35);
        }
    }

    public void move()
    {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for(int i = 0; i < pipes.size(); i++)
        {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }

            if(pipe.x + pipe.width < 0) {
                pipes.remove(pipe);
                i--;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    public boolean collision(Bird a, Pipe b)
    {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        // 🔊 GAME OVER SOUND LOGIC
        if (gameOver && !soundPlayed) {
            soundPlayed = true;

            if (bgMusic != null) {
                bgMusic.stop();
            }

            if (gameOverSound != null) {
                gameOverSound.setFramePosition(0);
                gameOverSound.start();
            }

            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            velocityY = -9;

            if (gameOver)
            {
                // restart
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;

                soundPlayed = false;

                if (bgMusic != null) {
                    bgMusic.setFramePosition(0);
                    bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
                }

                if (gameOverSound != null) {
                    gameOverSound.stop();
                    gameOverSound.setFramePosition(0);
                }

                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}