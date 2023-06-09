import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.util.HashMap;


public class GUI extends JPanel implements ActionListener, ChangeListener {
    private DefaultCategoryDataset chartDataset;
    private DefaultCategoryDataset chartDataset2;
    private Workbook workbook;
    private Sheet excelSheet;

    private static final long serialVersionUID = 1L;
    private Timer timer;
    private Board board;
    private JButton start;
    private JButton stop;
    private JButton clear;
    private JComboBox<Integer> drawType;
    private JSlider pred;
    private JFrame frame;
    private int iterNum = 0;
    private final int maxDelay = 500;
    private final int initDelay = 100;
    private boolean running = false;

    public GUI(JFrame jf) {
        frame = jf;
        timer = new Timer(initDelay, this);
        timer.stop();

        // Inicjalizacja datasetu dla wykresu
        chartDataset = new DefaultCategoryDataset();
        chartDataset2 = new DefaultCategoryDataset();

        // Inicjalizacja arkusza w pliku Excel
        workbook = new XSSFWorkbook();
        excelSheet = workbook.createSheet("Death Count");
    }

    public void initialize(Container container) {
        container.setLayout(new BorderLayout());
        container.setSize(new Dimension(1024, 768));

        JPanel buttonPanel = new JPanel();

        start = new JButton("Start");
        start.setActionCommand("Start");
        start.addActionListener(this);

        clear = new JButton("Calc Field");
        clear.setActionCommand("clear");
        clear.addActionListener(this);

        stop = new JButton("Stop");
        stop.setActionCommand("Stop");
        stop.addActionListener(this);

        pred = new JSlider();
        pred.setMinimum(0);
        pred.setMaximum(maxDelay);
        pred.addChangeListener(this);
        pred.setValue(maxDelay - timer.getDelay());

        drawType = new JComboBox<Integer>(Point.types);
        drawType.addActionListener(this);
        drawType.setActionCommand("drawType");

        buttonPanel.add(start);
        buttonPanel.add(clear);
        buttonPanel.add(drawType);
        buttonPanel.add(pred);
        buttonPanel.add(stop);

        board = new Board(1024, 768 - buttonPanel.getHeight());
        container.add(board, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(timer)) {
            iterNum++;
            frame.setTitle("Sound simulation (" + Integer.toString(iterNum) + " iteration)");
            board.iteration();
        } else {
            String command = e.getActionCommand();
            if (command.equals("Start")) {
                if (!running) {
                    timer.start();
                    start.setText("Pause");
                } else {
                    timer.stop();
                    start.setText("Start");
                }
                running = !running;
                clear.setEnabled(true);

            } else if (command.equals("clear")) {
                iterNum = 0;
                timer.stop();
                start.setEnabled(true);
                board.clear();
            } else if (command.equals("drawType")) {
                int newType = (Integer) drawType.getSelectedItem();
                board.editType = newType;
            } else if (command.equals("Stop")) {
                timer.stop();
                start.setEnabled(true);
                board.clear();
                createDeathCountExcel(board.map);
                createAgeChart();
                System.out.println(board.numOfDead);
                System.out.println(board.numOldYoungDead);
            }
        }
    }


    public void stateChanged(ChangeEvent e) {
        timer.setDelay(maxDelay - pred.getValue());
    }
    public void createAgeChart(){
        // Wyczyszczenie aktualnych danych w chartDataset
        chartDataset2.clear();
        float val1 = board.numChildDead / board.numOfDead;
        float val2 = board.numAdolescentDead / board.numOfDead;
        float val3 = board.numAdultDead / board.numOfDead;
        float val4 = board.numSeniorsDead / board.numOfDead;
        // Aktualizacja danych w chartDataset
        chartDataset2.addValue(val1, "Death Percentage", "Under 11");
        chartDataset2.addValue(val2, "Death Percentage", "11 to 18");
        chartDataset2.addValue(val3, "Death Percentage", "19 to 70");
        chartDataset2.addValue(val4, "Death Percentage", "Over 70");

        // Generowanie wykresu słupkowego
        JFreeChart barChart = ChartFactory.createBarChart(
                "Death Percentage", "Age group", "Death Percentage",
                chartDataset2, PlotOrientation.VERTICAL, true, true, false);

        // Konwersja wykresu na obrazek
        byte[] chartImageBytes;
        try {
            chartImageBytes = ChartUtils.encodeAsPNG(barChart.createBufferedImage(800, 600));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Dodawanie obrazka wykresu do panelu
        ImageIcon chartImageIcon = new ImageIcon(chartImageBytes);
        JLabel chartLabel = new JLabel(chartImageIcon);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        // Tworzenie ramki i dodawanie panelu z wykresem
        JFrame chartFrame = new JFrame("Death Percentage Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }

    public void createDeathCountExcel(HashMap<Integer, Integer> deathCountMap) {
        // Wyczyszczenie aktualnych danych w chartDataset
        chartDataset.clear();

        // Aktualizacja danych w chartDataset
        for (Integer key : deathCountMap.keySet()) {
            chartDataset.addValue(deathCountMap.get(key), "Death Count", String.valueOf(key));
        }

        // Generowanie wykresu słupkowego
        JFreeChart barChart = ChartFactory.createBarChart(
                "Death Count", "Iteration", "Death Count",
                chartDataset, PlotOrientation.VERTICAL, true, true, false);

        // Konwersja wykresu na obrazek
        byte[] chartImageBytes;
        try {
            chartImageBytes = ChartUtils.encodeAsPNG(barChart.createBufferedImage(800, 600));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // Dodawanie obrazka wykresu do panelu
        ImageIcon chartImageIcon = new ImageIcon(chartImageBytes);
        JLabel chartLabel = new JLabel(chartImageIcon);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        // Tworzenie ramki i dodawanie panelu z wykresem
        JFrame chartFrame = new JFrame("Death Count Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        chartFrame.pack();
        chartFrame.setVisible(true);


        // Zapisywanie danych do arkusza Excel
        int rowNum = 0;
        for (Integer key : deathCountMap.keySet()) {
            Row row = excelSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(key);
            row.createCell(1).setCellValue(deathCountMap.get(key));
        }

        // Zapis do pliku Excel
        try (FileOutputStream fileOut = new FileOutputStream("death_count.xlsx")) {
            workbook.write(fileOut);
            System.out.println("Plik Excel został pomyślnie zapisany.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
