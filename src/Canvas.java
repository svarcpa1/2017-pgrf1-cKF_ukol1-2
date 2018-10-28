import rasterdata.RasterImage;
import rasterdata.RasterImageBuffered;
import rasterops.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Function;

import javax.swing.*;

import static java.lang.Math.atan2;
import static java.lang.StrictMath.toDegrees;

/**
 * class for drawing on canvas: display pixel
 *
 * @author PGRF FIM UHK; Pavel Švarc
 * @version 2017
 */

public class Canvas {

	private final JFrame frame;
	private final JPanel panel;
	private final BufferedImage img;

	private RasterImage<Integer> rasterImage;
	private final LineRasterizer<Integer> liner;
	private final LineRasterizerDDA<Integer> linerDDA;
	private final LineRasterizerPolygon<Integer> linerPolygon;
	private final LineRasterizerCircle<Integer> linerCircle;
    private final LineRasterizerXiaolinWu lineXiaolin;
	private final FillerSeedFill<Integer> fillerSeedFill;
    private final FillerScanLine<Integer> fillerScanLine;
    private final LineRasterizerSquare<Integer> linerSquare;
    private final FillerScanLineClipping fillerScanLineClipping;
    private int previousX, previousY;
    private int previous2X, previous2Y;
    private int radius2;
    private int startAngle;
    //Lines (DDA, naive)
    private ArrayList<Line> linesListLines = new ArrayList<Line>();
    //PoligonMain
    private ArrayList<Line> polygonMainLinesList = new ArrayList<Line>();
    //trimmers lines and points
    private ArrayList<Line> trimmerLinesList = new ArrayList<Line>();
    private ArrayList<Point> apexListTrimmed = new ArrayList<>();
    private ArrayList<PointInDouble> apexListTrimmedDouble = new ArrayList<>();
    //clipper lines
    private ArrayList<Line> clipperLinesList = new ArrayList<Line>();

	public Canvas(final int width, final int height) {
		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setTitle("UHK FIM PGRF: " + this.getClass().getName() + " Pavel Švarc");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//GUI setup
        JToolBar jToolBarNorth = new JToolBar();
        JComboBox jComboBoxSelectType = new JComboBox();
        JLabel jLabelSelectType = new JLabel("Type:  ");
        JComboBox jComboBoxSelectColoring = new JComboBox();
        JLabel jLabelSelectColoring = new JLabel("     Coloring:  ");
        JLabel jLabelSpace = new JLabel("      ");
        JButton jButtonClear = new JButton("Clear canvas");
        JButton jButtonClipp = new JButton("Clip it");
        JButton jButtonSetClipper = new JButton("Set clipper");
        frame.add(jToolBarNorth, BorderLayout.NORTH);
        jToolBarNorth.setFloatable(false);
        jToolBarNorth.add(jLabelSelectType);
        jToolBarNorth.add(jComboBoxSelectType);
        jComboBoxSelectType.addItem("Line rasterization - Trivial algorithm (not completed)");
        jComboBoxSelectType.addItem("Line rasterization - DDA algorithm");
        jComboBoxSelectType.addItem("Line rasterization - Xiaolin Wu’s");
        jComboBoxSelectType.addItem("Polygon rasterization MAIN");
        jComboBoxSelectType.addItem("Circle rasterization");
        jComboBoxSelectType.addItem("Square rasterization");
        jComboBoxSelectType.addItem("Polygon rasterization (trimmed)");
        jToolBarNorth.add(jLabelSelectColoring);
        jToolBarNorth.add(jComboBoxSelectColoring);
        jComboBoxSelectColoring.addItem("Seed fill");
        jComboBoxSelectColoring.addItem("Seed fill - pattern");
        jComboBoxSelectColoring.addItem("Scan-line");
        jToolBarNorth.add(jLabelSpace);
        jToolBarNorth.add(jButtonSetClipper);
        jToolBarNorth.add(jButtonClipp);
        jToolBarNorth.add(jButtonClear);

        //BUTTON CLEAR listener
        jButtonClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
                polygonMainLinesList.clear();
                trimmerLinesList.clear();
                clipperLinesList.clear();
                linesListLines.clear();
                apexListTrimmedDouble.clear();
                apexListTrimmed.clear();
                panel.repaint();
            }
        });

        //BUTTON SET CLIPPER listener
        jButtonSetClipper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //clearing old, useless list
                linesListLines.clear();
                polygonMainLinesList.clear();
                trimmerLinesList.clear();
                clipperLinesList.clear();
                apexListTrimmedDouble.clear();
                apexListTrimmed.clear();

                //adding clipper lines
                clipperLinesList.add(new Line(0.5,0.1,0.1,0.7,0xFF8AC249));
                clipperLinesList.add(new Line(0.1,0.7,0.7,0.9,0xFF8AC249));
                clipperLinesList.add(new Line(0.7,0.9,0.5,0.1,0xFF8AC249));

                //clear canvas
                clear();

                //draw clipper
                for (int i = 0; i < clipperLinesList.size(); i++) {
                    rasterImage = linerDDA.rasterizeLine(rasterImage,
                            clipperLinesList.get(i).getX1(), clipperLinesList.get(i).getY1(),
                            clipperLinesList.get(i).getX2(), clipperLinesList.get(i).getY2(),
                            clipperLinesList.get(i).getColor());
                }

                jComboBoxSelectType.setSelectedIndex(6);

                panel.repaint();
            }
        });

        //BUTTON CLIP IT listener
        jButtonClipp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clipperLinesList.isEmpty()){
                    JOptionPane.showMessageDialog(null,
                            "No clipper entered use \"Set clipper\" button");
                }else {

                    if(trimmerLinesList.isEmpty()){
                        JOptionPane.showMessageDialog(null,
                                "No trimmer entered");
                    }else{
                        //recount trimmer points
                        for (int i = 0; i < apexListTrimmed.size(); i++) {
                            final double x1 = apexListTrimmed.get(i).getX() / (panel.getWidth() - 1.0);
                            final double y1 = 1 - apexListTrimmed.get(i).getY() / (panel.getHeight() - 1.0);
                            apexListTrimmedDouble.add(new PointInDouble(x1, y1));
                        }

                        ArrayList<Line> out = fillerScanLineClipping.clipp(clipperLinesList, apexListTrimmedDouble);

                        //fill out polygon
                        fillerScanLine.filler(rasterImage, 0xFFFF0000, out);

                        panel.repaint();
                    }
                }
            }
        });

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		rasterImage = new RasterImageBuffered<>(img,
				// toInteger: Function<PixelType, Integer>
                // anonymous class can be replaced with lambda
				new Function<Integer, Integer>() {
					@Override
					public Integer apply(Integer/*PixelType*/ value) {
						return value;
					}
				},
				// toPixelType: Function<Integer, PixelType>
				new Function<Integer, Integer>() {
					@Override
					public Integer/*PixelType*/ apply(Integer value) {
						return value;
					}
				});

		//init
		liner = new LineRasterizerNaive<>();
		linerDDA = new LineRasterizerDDA<>();
		linerPolygon = new LineRasterizerPolygon<>();
		linerCircle = new LineRasterizerCircle<>();
        lineXiaolin = new LineRasterizerXiaolinWu(rasterImage);
        fillerSeedFill = new FillerSeedFill<>();
        fillerScanLine = new FillerScanLine<>();
        linerSquare = new LineRasterizerSquare<>();
        fillerScanLineClipping = new FillerScanLineClipping();

		panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				present(g);
			}
		};

		panel.setPreferredSize(new Dimension(width, height));

		panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    if (jComboBoxSelectType.getSelectedIndex() == 0 ||
                            jComboBoxSelectType.getSelectedIndex() == 1 ||
                            jComboBoxSelectType.getSelectedIndex() == 3 ||
                            jComboBoxSelectType.getSelectedIndex() == 6) {

                        //recount points
                        final double startX = previousX / (panel.getWidth() - 1.0);
                        final double startY = 1 - previousY / (panel.getHeight() - 1.0);
                        final double endX = e.getX() / (panel.getWidth() - 1.0);
                        final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);

                        //trivial
                        if (jComboBoxSelectType.getSelectedIndex() == 0) {
                            linesListLines.add(new Line(startX, startY, endX, endY, 0xFF2095F2));
                        //dda
                        } else if (jComboBoxSelectType.getSelectedIndex() == 1) {
                            linesListLines.add(new Line(startX, startY, endX, endY, 0xFF8AC249));

                            //to rasterize dots
                            if((previousY==e.getY())&&(previousX==e.getX())){
                                rasterImage = linerDDA.rasterizeLine(rasterImage,
                                        startX, startY, endX, endY,
                                        0xFF8AC249);
                                panel.repaint();
                            }
                        }
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    previousX = e.getX();
                    previousY = e.getY();

                    //polygons
                    if (jComboBoxSelectType.getSelectedIndex() == 3 || (jComboBoxSelectType.getSelectedIndex() == 6 &&
                            (!clipperLinesList.isEmpty()))) {
                        //save points into apexLists
                        //saving into array in case of trimmed polygon
                        apexListTrimmed = linerPolygon.getApexListTrimmed();
                        ArrayList<Point> apexList = linerPolygon.getApexList();
                        //trimmer
                        if(jComboBoxSelectType.getSelectedIndex() == 6){
                            apexListTrimmed.add(new Point(e.getX(), e.getY()));
                        }else{
                            apexList.add(new Point(e.getX(), e.getY()));
                        }
                        Point point = new Point(e.getX(), e.getY());

                        //trimmer
                        if(jComboBoxSelectType.getSelectedIndex()==6){
                            if (apexListTrimmed.size() > 1) {
                                final double startX = previous2X / (panel.getWidth() - 1.0);
                                final double startY = 1 - previous2Y / (panel.getHeight() - 1.0);
                                final double endX = e.getX() / (panel.getWidth() - 1.0);
                                final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);
                                trimmerLinesList.add(new Line(startX, startY, endX, endY, 0xFF0000FF));
                                previous2X = e.getX();
                                previous2Y = e.getY();
                            } else {
                                previous2X = e.getX();
                                previous2Y = e.getY();
                            }
                        //main polygon
                        }else if (jComboBoxSelectType.getSelectedIndex()==3){
                            if (apexList.size() > 1) {
                                final double startX = previous2X / (panel.getWidth() - 1.0);
                                final double startY = 1 - previous2Y / (panel.getHeight() - 1.0);
                                final double endX = e.getX() / (panel.getWidth() - 1.0);
                                final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);
                                polygonMainLinesList.add(new Line(startX, startY, endX, endY, 0xFF8AC249));
                                previous2X = e.getX();
                                previous2Y = e.getY();
                            } else {
                                previous2X = e.getX();
                                previous2Y = e.getY();
                            }
                        }
                        //polygon
                        if (linerPolygon.getbeingRasterized()) {
                            //trimmer
                            if(jComboBoxSelectType.getSelectedIndex()==6){
                                if (((linerPolygon.getApexListTrimmed().get(0).x <= (point.x +
                                                linerPolygon.getDeviation())) &&
                                        (linerPolygon.getApexListTrimmed().get(0).x >= (point.x -
                                                linerPolygon.getDeviation())) &&
                                        (linerPolygon.getApexListTrimmed().get(0).y <= (point.y +
                                                linerPolygon.getDeviation())) &&
                                        (linerPolygon.getApexListTrimmed().get(0).y >= (point.y -
                                                linerPolygon.getDeviation())))) {

                                    final double startX = linerPolygon.getApexListTrimmed().get(0).x /
                                            (panel.getWidth() - 1.0);
                                    final double startY = 1 - linerPolygon.getApexListTrimmed().get(0).y /
                                            (panel.getHeight() - 1.0);
                                    final double endX = linerPolygon.getApexListTrimmed().
                                            get(linerPolygon.getApexListTrimmed().size() -
                                                    1).x /
                                            (panel.getWidth() - 1.0);
                                    final double endY =
                                            1 - linerPolygon.getApexListTrimmed().get(linerPolygon.
                                                    getApexListTrimmed().size() - 1).y
                                                    / (panel.getHeight() - 1.0);

                                    rasterImage = linerDDA.rasterizeLine(rasterImage,
                                            startX, startY, endX, endY,
                                            0xFF0000FF);
                                    trimmerLinesList.add(new Line(startX, startY, endX, endY, 0xFF0000FF));
                                    panel.repaint();
                                    linerPolygon.setApexListTrimmed(new ArrayList<Point>());
                                    linerPolygon.setbeingRasterized(false);
                                    return;
                                }
                            //clipper
                            }else if (jComboBoxSelectType.getSelectedIndex()==3){
                                if (((linerPolygon.getApexList().get(0).x <= (point.x +
                                                linerPolygon.getDeviation())) &&
                                        (linerPolygon.getApexList().get(0).x >= (point.x -
                                                linerPolygon.getDeviation())) &&
                                        (linerPolygon.getApexList().get(0).y <= (point.y +
                                                linerPolygon.getDeviation())) &&
                                        (linerPolygon.getApexList().get(0).y >= (point.y -
                                                linerPolygon.getDeviation())))){

                                    final double startX = linerPolygon.getApexList().get(0).x /(panel.getWidth() - 1.0);
                                    final double startY = 1 - linerPolygon.getApexList().get(0).y /
                                            (panel.getHeight() - 1.0);
                                    final double endX=linerPolygon.getApexList().get(linerPolygon.getApexList().size()-
                                            1).x /
                                            (panel.getWidth() - 1.0);
                                    final double endY =
                                            1 - linerPolygon.getApexList().get(linerPolygon.getApexList().size() - 1).y
                                                    / (panel.getHeight() - 1.0);

                                    rasterImage = linerDDA.rasterizeLine(rasterImage,
                                            startX, startY, endX, endY,
                                            0xFF8AC249);
                                    polygonMainLinesList.add(new Line(startX, startY, endX, endY, 0xFF8AC249));
                                    panel.repaint();
                                    linerPolygon.setApexList(new ArrayList<>());
                                    linerPolygon.setbeingRasterized(false);
                                    return;
                                }
                            }
                        }
                        linerPolygon.setbeingRasterized(true);
                    }else if ((jComboBoxSelectType.getSelectedIndex() == 6 &&
                            (clipperLinesList.isEmpty()))){
                        JOptionPane.showMessageDialog(null,
                                "No clipper entered use \"Set clipper\" button");
                    }

                    //circle
                    if (jComboBoxSelectType.getSelectedIndex() == 4 && linerCircle.getClickCounter() == 0) {
                        linerCircle.setCenterX(e.getX());
                        linerCircle.setCenterY(e.getY());
                        linerCircle.setBeingCircleRasterized(true);
                    }
                    if (jComboBoxSelectType.getSelectedIndex() == 4 &&
                            linerCircle.isBeingCircleRasterized() == true &&
                            linerCircle.getClickCounter() == 1) {

                        linerCircle.setBeingSectorRasterized(true);
                        linerCircle.setClickCounter(2);

                        startAngle = (int)toDegrees((atan2(e.getY(),e.getX())));
                    }
                    if((linerCircle.isBeingSectorRasterized()==true)&&
                            (linerCircle.isBeingCircleRasterized()==true)&&
                            (linerCircle.getClickCounter()==3)&&
                            (jComboBoxSelectType.getSelectedIndex()==4)) {
                        linerCircle.setClickCounter(0);
                        linerCircle.setBeingSectorRasterized(false);
                        linerCircle.setBeingCircleRasterized(false);
                    }

                    //square
                    if(jComboBoxSelectType.getSelectedIndex() == 5 &&(linerSquare.getClickCounter()==0)){
                        linerSquare.setSquareBeingRasterize(true);
                        linerSquare.setCenterX(e.getX());
                        linerSquare.setCenterY(e.getY());
                    }
                    if((jComboBoxSelectType.getSelectedIndex() == 5) && (linerSquare.getClickCounter()==1)){
                        linerSquare.setClickCounter(0);
                        linerSquare.setSquareBeingRasterize(false);
                    }
                }

                //filling
                if(SwingUtilities.isRightMouseButton(e)){
                    previousX = e.getX();
                    previousY = e.getY();

                    //seed fill
                    if(jComboBoxSelectColoring.getSelectedIndex()==0){
                        fillerSeedFill.filler(rasterImage,
                                previousX,previousY,
                                0xFFC4D4AF,0xFF8AC249);
                        panel.repaint();
                    }

                    //seed fill pattern
                    if(jComboBoxSelectColoring.getSelectedIndex()==1){
                        fillerSeedFill.fillerPattern(rasterImage,
                                previousX,previousY,
                                fillerSeedFill.getPATTERN(),0xFF8AC249);
                        panel.repaint();
                    }

                    //scan line
                    if(jComboBoxSelectColoring.getSelectedIndex()==2){
                        if(polygonMainLinesList.isEmpty()){
                            JOptionPane.showMessageDialog(null,
                                    "No polygon for scan line (main) entered!");
                        }else {
                            fillerScanLine.filler(rasterImage, 0xFFFF00FF, polygonMainLinesList);
                            panel.repaint();
                        }
                    }
                }
			}
		});

		panel.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
                    //naive Line
				    if (jComboBoxSelectType.getSelectedIndex() == 0) {
                        final double startX = previousX / (panel.getWidth() - 1.0);
                        final double startY = 1 - previousY / (panel.getHeight() - 1.0);
                        final double endX = e.getX() / (panel.getWidth() - 1.0);
                        final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);
                        clear();
                        for (int i = 0; i < linesListLines.size(); i++) {
                            if (linesListLines.get(i).getColor() == 0x2095F2) {
                                rasterImage = liner.rasterizeLine(rasterImage,
                                        linesListLines.get(i).getX1(), linesListLines.get(i).getY1(),
                                        linesListLines.get(i).getX2(), linesListLines.get(i).getY2(),
                                        linesListLines.get(i).getColor());
                            } else if (linesListLines.get(i).getColor() == 0xFF8AC249) {
                                rasterImage = linerDDA.rasterizeLine(rasterImage,
                                        linesListLines.get(i).getX1(), linesListLines.get(i).getY1(),
                                        linesListLines.get(i).getX2(), linesListLines.get(i).getY2(),
                                        linesListLines.get(i).getColor());
                            }
                        }
                        for (int i = 0; i < polygonMainLinesList.size(); i++) {
                            rasterImage = linerDDA.rasterizeLine(rasterImage,
                                    polygonMainLinesList.get(i).getX1(), polygonMainLinesList.get(i).getY1(),
                                    polygonMainLinesList.get(i).getX2(), polygonMainLinesList.get(i).getY2(),
                                    polygonMainLinesList.get(i).getColor());
                        }
                        rasterImage = liner.rasterizeLine(rasterImage, startX, startY, endX, endY, 0xFF2095F2);
                        panel.repaint();
                    }
                    //DDA Line
                    if (jComboBoxSelectType.getSelectedIndex() == 1) {
                        final double startX = previousX / (panel.getWidth() - 1.0);
                        final double startY = 1 - previousY / (panel.getHeight() - 1.0);
                        final double endX = e.getX() / (panel.getWidth() - 1.0);
                        final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);
                        clear();
                        for (int i = 0; i < linesListLines.size(); i++) {
                            if (linesListLines.get(i).getColor() == 0x2095F2) {
                                rasterImage = liner.rasterizeLine(rasterImage,
                                        linesListLines.get(i).getX1(), linesListLines.get(i).getY1(),
                                        linesListLines.get(i).getX2(), linesListLines.get(i).getY2(),
                                        linesListLines.get(i).getColor());
                            } else if (linesListLines.get(i).getColor() == 0xFF8AC249) {
                                rasterImage = linerDDA.rasterizeLine(rasterImage,
                                        linesListLines.get(i).getX1(), linesListLines.get(i).getY1(),
                                        linesListLines.get(i).getX2(), linesListLines.get(i).getY2(),
                                        linesListLines.get(i).getColor());
                            }
                        }
                        for (int i = 0; i < polygonMainLinesList.size(); i++) {
                            rasterImage = linerDDA.rasterizeLine(rasterImage,
                                    polygonMainLinesList.get(i).getX1(), polygonMainLinesList.get(i).getY1(),
                                    polygonMainLinesList.get(i).getX2(), polygonMainLinesList.get(i).getY2(),
                                    polygonMainLinesList.get(i).getColor());
                        }

                        rasterImage = linerDDA.rasterizeLine(rasterImage,
                                startX, startY, endX, endY,
                                0xFF8AC249);
                        panel.repaint();
                    }
                    //Xiaolin Line
                    if (jComboBoxSelectType.getSelectedIndex() == 2) {
                        clear();
                        rasterImage = lineXiaolin.rasterizeLine(previousX, previousY, e.getX(), e.getY());
                        panel.repaint();
                    }
                }
			}
            @Override
            public void mouseMoved(MouseEvent e) {
            //polygon
			    //trimmer
                if(jComboBoxSelectType.getSelectedIndex()==6){
                    if (linerPolygon.getbeingRasterized()) {
                        final double startX = previousX / (panel.getWidth() - 1.0);
                        final double startY = 1 - previousY / (panel.getHeight() - 1.0);
                        final double endX = e.getX() / (panel.getWidth() - 1.0);
                        final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);
                        clear();
                        //clipper solid
                        for (int i = 0; i < clipperLinesList.size(); i++) {
                            rasterImage = linerDDA.rasterizeLine(rasterImage,
                                    clipperLinesList.get(i).getX1(), clipperLinesList.get(i).getY1(),
                                    clipperLinesList.get(i).getX2(), clipperLinesList.get(i).getY2(),
                                    clipperLinesList.get(i).getColor());
                        }

                        for (int i = 0; i < trimmerLinesList.size(); i++) {
                                rasterImage = linerDDA.rasterizeLine(rasterImage,
                                        trimmerLinesList.get(i).getX1(), trimmerLinesList.get(i).getY1(),
                                        trimmerLinesList.get(i).getX2(), trimmerLinesList.get(i).getY2(),
                                        trimmerLinesList.get(i).getColor());
                        }
                        rasterImage = linerDDA.rasterizeLine(rasterImage,
                                startX, startY, endX, endY,
                                0xFF0000FF);
                        panel.repaint();
                    }
			    }else if(jComboBoxSelectType.getSelectedIndex()==3) {
                    if (linerPolygon.getbeingRasterized()) {
                        final double startX = previousX / (panel.getWidth() - 1.0);
                        final double startY = 1 - previousY / (panel.getHeight() - 1.0);
                        final double endX = e.getX() / (panel.getWidth() - 1.0);
                        final double endY = 1 - e.getY() / (panel.getHeight() - 1.0);
                        clear();
                        for (int i = 0; i < linesListLines.size(); i++) {
                            if (linesListLines.get(i).getColor() == 0x2095F2) {
                                rasterImage = liner.rasterizeLine(rasterImage,
                                        linesListLines.get(i).getX1(), linesListLines.get(i).getY1(),
                                        linesListLines.get(i).getX2(), linesListLines.get(i).getY2(),
                                        linesListLines.get(i).getColor());
                            } else if (linesListLines.get(i).getColor() == 0xFF8AC249) {
                                rasterImage = linerDDA.rasterizeLine(rasterImage,
                                        linesListLines.get(i).getX1(), linesListLines.get(i).getY1(),
                                        linesListLines.get(i).getX2(), linesListLines.get(i).getY2(),
                                        linesListLines.get(i).getColor());
                            }
                        }
                        for (int i = 0; i < polygonMainLinesList.size(); i++) {
                            rasterImage = linerDDA.rasterizeLine(rasterImage,
                                    polygonMainLinesList.get(i).getX1(), polygonMainLinesList.get(i).getY1(),
                                    polygonMainLinesList.get(i).getX2(), polygonMainLinesList.get(i).getY2(),
                                    polygonMainLinesList.get(i).getColor());
                        }
                        rasterImage = linerDDA.rasterizeLine(rasterImage,
                                startX, startY, endX, endY,
                                0xFF8AC249);
                        panel.repaint();
                    }
                }

                //circle - drawing radius
                if(linerCircle.isBeingCircleRasterized()==true && linerCircle.isBeingSectorRasterized()==false){
                    radius2 = (int) Math.sqrt( Math.pow(e.getX()-linerCircle.getCenterX(),2)
                            + Math.pow(e.getY()-linerCircle.getCenterY(),2));
                    clear();
                    rasterImage = linerCircle.rasterizeCircle(rasterImage,
                            linerCircle.getCenterX(),linerCircle.getCenterY(),radius2,
                            0xFFf70fff);
                    panel.repaint();
                    linerCircle.setClickCounter(1);
                }
                //circle - drawing sector
                if (linerCircle.isBeingSectorRasterized()==true && linerCircle.isBeingCircleRasterized()==true){
                    linerCircle.setClickCounter(3);
                    clear();
                    rasterImage.withPixel((int)linerCircle.getCenterX(),(int)linerCircle.getCenterY(),0xFFf70fff);
                    rasterImage = linerCircle.rasterizeCircleSector(rasterImage,
                            linerCircle.getCenterX(), linerCircle.getCenterY(),
                            radius2,
                            0,
                            ((int) Math.sqrt( Math.pow(e.getX()-linerCircle.getCenterX(),2)
                                    + Math.pow(e.getY()-linerCircle.getCenterY(),2))),
                            0xFF8AC249);
                    panel.repaint();
                }

                //square drawing
                if(linerSquare.isSquareBeingRasterize()){
                    clear();
                    linerSquare.setClickCounter(1);
                    rasterImage = linerSquare.rasterizeSquare(rasterImage,
                            linerSquare.getCenterX(),linerSquare.getCenterY(),
                            ((int) Math.sqrt( Math.pow(e.getX()-linerSquare.getCenterX(),2)
                                    + Math.pow(e.getY()-linerSquare.getCenterY(),2))),
                            0xFF8AC249);
                    panel.repaint();
                }
            }
		});

		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public void clear() {
		rasterImage = rasterImage.cleared(0xFF2f2f2f);
	}

	public void present(final Graphics graphics) {
		graphics.drawImage(img, 0, 0, null);
	}

	public void draw() {
		clear();
		//saying hallo
		rasterImage = linerDDA.rasterizeLine(rasterImage,
				0.175, 0.1,0.175, 0.25,
				0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.275, 0.1,0.275, 0.25,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.325, 0.1,0.325, 0.25,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.475, 0.1,0.475, 0.25,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.625, 0.1,0.625, 0.25,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.175, 0.17,0.275, 0.17,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.325, 0.17,0.425, 0.17,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.325, 0.25,0.425, 0.25,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.325, 0.1,0.425, 0.1,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.475, 0.1,0.575, 0.1,
                0xFF008000);
        rasterImage = linerDDA.rasterizeLine(rasterImage,
                0.625, 0.1,0.725, 0.1,
                0xFF008000);
		rasterImage = linerCircle.rasterizeCircle(rasterImage,
                656,490,50,
                0xFF8AC249);
        rasterImage = linerCircle.rasterizeCircleSector(rasterImage,
                656,490,70,0,180,
                0xFFf0f0f0);
        rasterImage = linerSquare.rasterizeSquare(rasterImage,
                180,495,55,
                0xFF8AC249);
	}

	public void start() {
		draw();
		panel.repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Canvas(800, 600)::start);
	}
}