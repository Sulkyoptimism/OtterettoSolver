package ScreenToString;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

public class ScreenToString {
    // Compulsory
    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }


    //This takes an image then finds the largest rectangle closest to the original size and selects that as the main
    //section. This means that as long as the picture has no other bounding box bar the game then the
    //image detection will work. it find the bounds then divides the image by the grid space we have defined, then
    //check these images to see if they are "busy" eg containing many lines, from there it then compares the grey scale
    //to detect which colour we are looking at, everything else is filled with 'O's as they are open tiles.
    //it then creates a string representation of the board for use by the solver.
    public static String run(String Path){
        //Load Image
        Mat src = Imgcodecs.imread(Path);


        //GreyScale Image
        Mat grey = new Mat();
        Size size = new Size(3, 3);
        double[][] sharpenKernel = { { -1, -1, -1 }, { -1, 9, -1 }, { -1, -1, -1 } };
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size);
        Imgproc.cvtColor(src, grey, Imgproc.COLOR_BGR2GRAY);
        //showWaitDestroy("grey", grey);

        Mat board = getBoardFromImage(grey);
        //showWaitDestroy("board", board);

        int xtemp = board.width();
        int ytemp = board.height();

        Rect cropB = new Rect((int)(xtemp*0.025), (int)(ytemp*0.006), (int)(xtemp*0.97), (int)(ytemp*0.985));
        Mat tightBoard = new Mat(board,cropB);
        //showWaitDestroy("tightBoard", tightBoard);

        ArrayList<Rect> Rois = getGrid(tightBoard);
        ArrayList<Mat> RoiImages = processROIs(Rois, tightBoard);


        ArrayList<Mat> newRois = new ArrayList<>();

        //System.out.print("Pass\n");
        for (int i = 0; i < RoiImages.size(); i++) {
            int x_size = RoiImages.get(i).width();
            int y_size = RoiImages.get(i).height();

            Rect crop = new Rect((int)(x_size*0.2), (int)(y_size*0.2), (int)(x_size*0.6), (int)(y_size*0.6));
            Mat image_roi = new Mat(RoiImages.get(i),crop);
            newRois.add(image_roi);
            //showWaitDestroy("new roi", image_roi);

        }

        Stack<String> rows = new Stack<>();
        StringBuilder sb = new StringBuilder();

        int count = 0;

        for (int i = 0; i < 40; i++) {
            if(newRois.get(i) == null){
                sb.append('O');
            }
            else{
                Mat threshed = new Mat();
                Imgproc.threshold(newRois.get(i), threshed, 82, 255, Imgproc.THRESH_OTSU); //Black tile, black Piece
                //showWaitDestroy("threshed", threshed);

                Mat blur = new Mat();
                Imgproc.medianBlur(threshed, blur, 5);
                //showWaitDestroy("blur", blur);

                Mat edges = new Mat();
                Imgproc.Canny(threshed, edges, 0, 100);


                Mat lines = new Mat();
                Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 1);
                //System.out.println("Lines len: " + lines.rows() + " index: " + i + "\n");


                if(lines.rows() > 35){
                    //find colour
                    double[] temp = newRois.get(i).get(10,10);
                    //System.out.print("help\n");
                    //Purple - 122
                    //Yellow - 188
                    //Green - 145
                    //Red - 110
                    //Blue - 127

                    if(105 < temp[0] && temp[0] < 115){
                        sb.append('R');
                    }
                    else if(115 < temp[0] && temp[0] < 125){
                        sb.append('P');
                    }
                    else if(125 < temp[0] && temp[0] < 130){
                        sb.append('B');
                    }
                    else if(135 < temp[0] && temp[0] < 148){
                        sb.append('G');
                    }
                    else if(150 < temp[0] ){
                        sb.append('Y');
                    }
                    else{
                        sb.append('O');
                    }

                }
                else{
                    //add O
                    sb.append('O');
                }
            }


            if(count==3){
                rows.push(sb.toString());
                sb.delete(0, sb.toString().length());
                count=0;
            }
            else{
                count++;

            }

        }
        StringBuilder outSB = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            outSB.append(rows.pop());
        }

        String out = outSB.toString();
        //System.out.print(out + "\n");
        //System.out.print(out + "\n");
        System.out.print("Done ScreenCapping\n");
        return out;
    }

    public static ArrayList<Rect> getGrid(Mat inputBoard)
    {
        ArrayList<Rect> out = new ArrayList<>();
        int adjustedWidth = inputBoard.cols()/4;
        int adjustedHeight = inputBoard.rows()/10;
        for(int r = 0; r < 10; r ++)
        {
            for(int c = 0; c < 4; c ++)
            {
                Rect rec = new Rect(c*(adjustedWidth), r*(adjustedHeight), adjustedWidth, adjustedHeight);

                out.add(rec);
            }
        }
        return out;
    }

    public static Mat getBoardFromImage(Mat image)
    {
        Mat input = image;

        while(true)
        {

            Mat threshed = new Mat();

            Imgproc.threshold(input, threshed, 5, 255, Imgproc.THRESH_OTSU);
            //showWaitDestroy("test",threshed);
            Mat canny = new Mat();
            Imgproc.Canny(threshed, canny, 50, 400);
            //showWaitDestroy("test",canny);

            double ogArea = canny.size().height * canny.size().width;
            Mat check = getMaxROI(canny, image, ogArea);
            if(check != null)
            {
                input = check;
            }
            else {
                break;
            }
        }
        return input;
    }


    public static Mat getMaxROI(Mat edgesInput, Mat sourceInput, double ogSize)
    {
        Mat hi = new Mat();
        List<MatOfPoint> pointsMat = new ArrayList<MatOfPoint>();
        Imgproc.findContours(edgesInput, pointsMat, hi, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        //System.out.println("number of points" + pointsMat.size());

        double maxArea = 0;
        Rect maxRec = new Rect();

        for (MatOfPoint mop : pointsMat) {
            //System.out.println(mop);
            double area = Imgproc.contourArea(mop);
            Rect rec = Imgproc.boundingRect(mop);
            Mat roiTest= new Mat(sourceInput, rec);
            //showWaitDestroy("roiTest",roiTest);
            if(area> maxArea )
            {
                maxArea = area;
                maxRec = rec;
            }
        }
        //System.out.println("MaxArea: "  + maxArea);
        //System.out.println("og sizze : "  + ogSize);
        if (maxArea == ogSize || maxArea > ogSize*0.99){
            //System.out.println("What even");
            return null;
        }
        else if(maxArea > ogSize*0.5 && maxArea <= ogSize*0.9){

            // System.out.println("og sizze boundary: "  + ogSize*0.80);
            Mat roiM = new Mat(sourceInput, maxRec);
            return roiM;
        }
        //System.out.println("more wtf");
        return null;


    }

    public static ArrayList<Mat> processROIs(ArrayList<Rect> Rois, Mat sourceInput)
    {
        ArrayList<Mat> out = new ArrayList<>();
        for(Rect rec : Rois)
        {
            Mat outMat = new Mat(sourceInput, rec);
            out.add(outMat);
            //showWaitDestroy("temp out", outMat);
        }
        return out;
    }

    private static void showWaitDestroy(String winname, Mat img) {
        HighGui.imshow(winname, img);
        HighGui.moveWindow(winname, 500, 0);
        HighGui.waitKey(0);
        HighGui.destroyWindow(winname);
    }
}