package rasterops;

import java.util.ArrayList;

/**
 * class for representing Sutherland–Hodgman's algorithm - trimming polygon
 */
public class FillerScanLineClipping{

    private ArrayList<PointInDouble> outPointList = new ArrayList<>();
    private ArrayList<Line> linesFromPoints = new ArrayList<>();

    /**
     * class with Sutherland–Hodgman's algorithm
     * @param clipperLineList list of clipper's lines
     * @param trimmedPointList list of trimmer's lines
     * @return list of out polygon's lines (lines will be used in canvas to fill by scan-line)
     */
    public ArrayList<Line> clipp (ArrayList<Line> clipperLineList,
                                         ArrayList<PointInDouble> trimmedPointList){

        //cheaper than clear
        linesFromPoints= new ArrayList<>();

        //main algorithm
        for(Line edge: clipperLineList){
            outPointList = new ArrayList<>();
            int last = trimmedPointList.size()-1;
            PointInDouble v1 = trimmedPointList.get(last);
            for (PointInDouble v2: trimmedPointList){
                if (isInsideEdge(v2,edge)){
                    if(!isInsideEdge(v1,edge)){
                        outPointList.add(getIntersection(v1,v2,edge));
                    }
                    outPointList.add(v2);
                }else {
                    if(isInsideEdge(v1,edge)){
                        outPointList.add(getIntersection(v1,v2,edge));
                    }
                }
                v1=v2;
            }
            trimmedPointList=outPointList;
        }

        //making lines out of points
        for(int x=0; x< (outPointList.size()-1); x++){
            double x1 = outPointList.get(x).getX();
            double y1 = outPointList.get(x).getY();
            double x2 = outPointList.get(x+1).getX();
            double y2 = outPointList.get(x+1).getY();

            linesFromPoints.add(new Line(x1,y1,x2,y2,0xFF0000));
        }

        //connecting last point with first
        linesFromPoints.add(new Line(outPointList.get(0).getX(),
                outPointList.get(0).getY(),
                outPointList.get(outPointList.size()-1).getX(),
                outPointList.get(outPointList.size()-1).getY(),
                0xFF0000));

        return linesFromPoints;
    }

    /**
     * method to know if is point out, inside or on the edge
     * @param point point which I wanna compare
     * @param line line to compare
     * @return true/false due to position of point due to line
     */
    public boolean isInsideEdge(PointInDouble point, Line line){
        double tmp = ((line.getY2()-line.getY1())*point.getX())-((line.getX2()-line.getX1())*point.getY())+
                (line.getX2()*line.getY1()-line.getY2()*line.getX1());
        return tmp >= 0;
    }

    /**
     * method to count coordinates of intersection (apex)
     * @param point1 start point of first line
     * @param point2 end point of first line
     * @param line second line
     * @return point (intersection) or apex of result polygon
     */
    public PointInDouble getIntersection(PointInDouble point1, PointInDouble point2, Line line){
        double x1 = line.getX1();
        double x2 = line.getX2();
        double x3 = point1.getX();
        double x4 = point2.getX();

        double y1 = line.getY1();
        double y2 = line.getY2();
        double y3 = point1.getY();
        double y4 = point2.getY();

        double x = (((x1*y2)-x2*y1)*(x3-x4)-(x3*y4-x4*y3)*(x1-x2))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
        double y = ((x1*y2-x2*y1)*(y3-y4)-(x3*y4-x4*y3)*(y1-y2))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));

        PointInDouble result = new PointInDouble(x,y);
        return result;
    }
}
