import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class GridSolver extends JPanel {
    private int Rowsnum;
    private int Colsnum;
    private static final int Sizeofcell = 50;
    private static final Color Blocked_color = Color.BLACK;
    private static final Color Start_color = Color.GREEN;
    private static final Color Finsh_color = Color.RED;
    private static final Color Path_color = Color.YELLOW;
    private static final Color Grid_color_line = Color.RED;

    private boolean[][] grid;
    private int startX = -1, startY = -1;
    private ArrayList<Point> targets;
    private boolean[][] blocked;
    private boolean[][] visited;
    private int[][] costs;
    private int[][] heuristic;
    private Node[][] parents;
    private ArrayList<Point> path;
    private boolean isEuclidean = true; // Default to Euclidean heuristic
    private int steps = 0;
    private int testedNodes = 0;

    public GridSolver(int Rowsnum, int Colsnum) {
        this.Rowsnum = Rowsnum;
        this.Colsnum = Colsnum;
        grid = new boolean[Rowsnum][Colsnum];
        blocked = new boolean[Rowsnum][Colsnum];
        visited = new boolean[Rowsnum][Colsnum];
        costs = new int[Rowsnum][Colsnum];
        heuristic = new int[Rowsnum][Colsnum];
        parents = new Node[Rowsnum][Colsnum];
        path = new ArrayList<>();
        targets = new ArrayList<>();

        setPreferredSize(new Dimension(Colsnum * Sizeofcell, Rowsnum * Sizeofcell));
        setBackground(Color.WHITE);

        addMouseListener(new GridMouseListener());

        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(new SolveButtonListener());
        add(solveButton);

        // Add toggle button for heuristic selection
        JToggleButton heuristicToggleButton = new JToggleButton("is Euclidean ");
        heuristicToggleButton.addActionListener(new HeuristicToggleListener());
        add(heuristicToggleButton);
    }

    private class GridMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX() / Sizeofcell;
            int y = e.getY() / Sizeofcell;

            if (SwingUtilities.isLeftMouseButton(e)) {
                // Check if the clicked cell is not the start or target cell
                if (!((x == startX && y == startY) || targets.contains(new Point(x, y)))) {
                    grid[x][y] = true; // Mark as blocked
                    blocked[x][y] = true;
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                if (startX == -1) {
                    startX = x;
                    startY = y;
                } else if (targets.size() < 2) {
                    targets.add(new Point(x, y));
                }
            }
            repaint();
        }
    }

    private class SolveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            solve();
            repaint();
        }
    }

    private class HeuristicToggleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            isEuclidean = !isEuclidean;
            repaint(); // Repaint the grid to reflect the updated heuristic
        }
    }

    private void solve() {
        steps = 0; // Reset steps
        testedNodes = 0; // Reset tested nodes

        // Clear existing path
        path.clear();
//hur fro all high and not visited
        for (int i = 0; i < Rowsnum; i++) {
            for (int j = 0; j < Colsnum; j++) {
                visited[i][j] = false;
                costs[i][j] = Integer.MAX_VALUE;
                if (isEuclidean) {
                    heuristic[i][j] = Integer.MAX_VALUE; // Initialize to max value for Euclidean heuristic
                } else {
                    heuristic[i][j] = Integer.MAX_VALUE; // Initialize to max value for Manhattan heuristic
                }
                parents[i][j] = null;
            }
        }
//add the node that i am in it
        PriorityQueue<Node> open = new PriorityQueue<>();
        open.add(new Node(startX, startY, 0, heuristic[startX][startY]));

        while (!open.isEmpty()) {
            Node curr = open.poll();
          //  removes the node with the lowest f
            int x = curr.x;
            int y = curr.y;
//check if the node in open is target
            for (Point target : targets) {
                if (x == target.x && y == target.y) {
                    bulid_path(x, y);
                    return; // Found the nearest target, exit the loop
                }
            }
//make it visited
            visited[x][y] = true;
            testedNodes++; // Increment tested nodes

            // see  neighbors
            Neighbor(x - 1, y, curr, open);
            Neighbor(x + 1, y, curr, open);
            Neighbor(x, y - 1, curr, open);
            Neighbor(x, y + 1, curr, open);
            /*// see  neighbors this get the proirtiy for the under one
            Neighbor(x, y + 1, curr, open);
            Neighbor(x + 1, y, curr, open);
            Neighbor(x - 1, y, curr, open);
            Neighbor(x, y - 1, curr, open);*/
        }
    }
//give from open Neighbor and dont go out border
    private void Neighbor(int xx, int yy, Node curr, PriorityQueue<Node> open) {
        if (xx >= 0 && xx < Rowsnum && yy >= 0 && yy < Colsnum
                && !visited[xx][yy] && !blocked[xx][yy]) {
            int newcost = curr.cost + 1; // add cost for the current node
            if (newcost < costs[xx][yy]) {
                costs[xx][yy] = newcost;
                parents[xx][yy] = curr;
                int heuristicValue;
                if (isEuclidean) {
                    heuristicValue =  Euclidean_calc(xx, yy);//calc hur for each one 
                    
                } else {
                    heuristicValue =  Manhattan_calc(xx, yy);
                }
                heuristic[xx][yy] = heuristicValue;
                open.add(new Node(xx, yy, newcost, heuristicValue));
            }
        }
    }
//which target is nerest to me 
    private int Euclidean_calc(int x, int y) {
        int minhur = Integer.MAX_VALUE;
        for (Point target : targets) {
            int hur = (int) Math.sqrt(Math.pow(target.x - x, 2) + Math.pow(target.y - y, 2));
            if (hur < minhur) {
                minhur = hur;
                //min for which target
            }
        }
        return minhur;
    }

    private int Manhattan_calc(int x, int y) {
        int minhur = Integer.MAX_VALUE;
        for (Point target : targets) {
            int hur = Math.abs(target.x - x) + Math.abs(target.y - y);
            if (hur < minhur) {
                minhur = hur;
                //min for which target
            }
        }
        return minhur;
    }

    private void bulid_path(int targetX, int targetY) {
        path.clear();
        Node curr = parents[targetX][targetY];
        while (curr != null&& (curr.x != startX || curr.y != startY)) {
            path.add(new Point(curr.x, curr.y));
            curr = parents[curr.x][curr.y];
        }
// from target to perant is array to get the path 
        // Calculate steps
        steps = path.size();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < Rowsnum; i++) {
            for (int j = 0; j < Colsnum; j++) {
                if (grid[i][j]) {
                    g.setColor(Blocked_color);
                    g.fillRect(i * Sizeofcell, j * Sizeofcell, Sizeofcell, Sizeofcell);
                }
                g.setColor(Grid_color_line);
                g.drawRect(i * Sizeofcell, j * Sizeofcell, Sizeofcell, Sizeofcell);
            }
        }

        if (startX != -1) {
            g.setColor(Start_color);
            g.fillRect(startX * Sizeofcell, startY * Sizeofcell, Sizeofcell, Sizeofcell);
        }
//we do it recarive so will paint paernt
        for (Point target : targets) {
            g.setColor(Finsh_color);
            g.fillRect(target.x * Sizeofcell, target.y * Sizeofcell, Sizeofcell, Sizeofcell);
        }

        g.setColor(Path_color);
        for (Point point : path) {
            g.fillRect(point.x * Sizeofcell, point.y * Sizeofcell, Sizeofcell, Sizeofcell);
        }

        // Display heuristic values based on selection
        for (int i = 0; i < Rowsnum; i++) {
            for (int j = 0; j < Colsnum; j++) {
                if (isEuclidean) {
                    int alpha = Math.min(255, Math.max(0, heuristic[i][j] * 20)); // Clamp alpha value to range [0, 255]
                    g.setColor(new Color(0, 100, 255, alpha)); // Blue color for Euclidean heuristic
                } else {
                    int alpha = Math.min(255, Math.max(0, heuristic[i][j] * 20)); // Clamp alpha value to range [0, 255]
                    g.setColor(new Color(100, 255, 100, alpha)); // Yellow color for Manhattan heuristic
                }

                g.fillRect(i * Sizeofcell, j * Sizeofcell, Sizeofcell, Sizeofcell);
            }
        }

        // Display steps and tested nodes
        g.setColor(Color.BLACK);
        g.drawString("Steps to reach target: " + steps, 10, getHeight() - 30);
        g.drawString("Tested nodes: " + testedNodes, 10, getHeight() - 15);
    }

    private static class Node implements Comparable<Node> {
        int x, y;
        int cost;
        int heuristics;

        Node(int x, int y, int cost, int heuristics) {
            this.x = x;
            this.y = y;
            this.cost = cost; //cost for current
            this.heuristics = heuristics;
        }

        @Override
        public int compareTo(Node other) {
            int fThis = this.cost + this.heuristics;
            int fOther = other.cost + other.heuristics;
            return Integer.compare(fThis, fOther);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Grid Solver");
            int Rowsnum = getNumericInput("Enter Number of Rows");
            int Colsnum = getNumericInput("Enter Number of Columns");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new GridSolver(Rowsnum, Colsnum), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static int getNumericInput(String message) {
        String input = JOptionPane.showInputDialog(null, message);
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return getNumericInput(message);
        }
    }
}
