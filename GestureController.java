import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import java.util.*;

public class GestureController {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    enum Action {
        SHOOT, MOVE_RIGHT, JUMP, STOP
    }

    static Queue<Action> actionBuffer = new LinkedList<>();
    static final int BUFFER_SIZE = 5;

    public static void main(String[] args) {
        VideoCapture cap = new VideoCapture(0);
        if (!cap.isOpened()) {
            System.out.println("Cannot open camera");
            return;
        }

        Mat frame = new Mat();
        Mat hsv = new Mat();
        Mat mask = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

        while (true) {
            cap.read(frame);
            if (frame.empty()) break;

            Core.flip(frame, frame, 1);
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

            // Adjusted skin HSV range
            Scalar lower = new Scalar(0, 30, 60);
            Scalar upper = new Scalar(20, 150, 255);
            Core.inRange(hsv, lower, upper, mask);

            Imgproc.GaussianBlur(mask, mask, new Size(9, 9), 2);
            Imgproc.erode(mask, mask, kernel);
            Imgproc.dilate(mask, mask, kernel);
            Imgproc.medianBlur(mask, mask, 5);

            HighGui.imshow("Mask", mask);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            Action action = Action.STOP;

            if (!contours.isEmpty()) {
                int maxIndex = 0;
                double maxArea = 0;

                for (int i = 0; i < contours.size(); i++) {
                    double area = Imgproc.contourArea(contours.get(i));
                    if (area > maxArea) {
                        maxArea = area;
                        maxIndex = i;
                    }
                }

                MatOfPoint handContour = contours.get(maxIndex);
                MatOfInt hull = new MatOfInt();
                Imgproc.convexHull(handContour, hull, false);

                MatOfInt4 defects = new MatOfInt4();
                if (handContour.total() >= 3 && hull.total() >= 3) {
                    Imgproc.convexityDefects(handContour, hull, defects);
                }

                int fingerCount = 0;

                if (!defects.empty()) {
                    List<Point> contourList = handContour.toList();
                    int[] defectArray = defects.toArray();

                    for (int i = 0; i < defectArray.length; i += 4) {
                        int startIdx = defectArray[i];
                        int endIdx = defectArray[i + 1];
                        int farIdx = defectArray[i + 2];
                        float depth = defectArray[i + 3] / 256.0f;

                        Point start = contourList.get(startIdx);
                        Point end = contourList.get(endIdx);
                        Point far = contourList.get(farIdx);

                        double angle = getAngle(start, far, end);

                        if (depth > 20 && angle < 90) {
                            fingerCount++;
                            Imgproc.circle(frame, far, 5, new Scalar(0, 0, 255), -1);
                        }
                    }
                }

                // Map finger count to action
                if (fingerCount == 0) action = Action.MOVE_RIGHT;
                else if (fingerCount == 1) action = Action.SHOOT;
                else if (fingerCount == 2) action = Action.JUMP;
                else action = Action.STOP;

                // Add to smoothing buffer
                actionBuffer.add(action);
                if (actionBuffer.size() > BUFFER_SIZE)
                    actionBuffer.poll();

                // Get most frequent action from buffer
                Action stableAction = getMostFrequentAction(actionBuffer);

                Imgproc.drawContours(frame, contours, maxIndex, new Scalar(0, 255, 0), 2);
                Imgproc.putText(frame, "Action: " + stableAction, new Point(20, 50),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 2);
                Imgproc.putText(frame, "Fingers: " + fingerCount, new Point(20, 90),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(100, 255, 100), 2);

                System.out.println("Detected Action: " + stableAction);
            }

            HighGui.imshow("Gesture Controller", frame);
            if (HighGui.waitKey(10) == 27) break;
        }

        cap.release();
        HighGui.destroyAllWindows();
    }

    // Helper to get angle between 3 points
    private static double getAngle(Point s, Point f, Point e) {
        double a = distance(f, e);
        double b = distance(f, s);
        double c = distance(e, s);
        return Math.acos((b*b + a*a - c*c) / (2 * b * a)) * (180 / Math.PI);
    }

    private static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    // Get most common action in buffer
    private static Action getMostFrequentAction(Queue<Action> buffer) {
        Map<Action, Integer> freq = new HashMap<>();
        for (Action a : buffer) {
            freq.put(a, freq.getOrDefault(a, 0) + 1);
        }
        return Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
