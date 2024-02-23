import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BruteForce {
    private List<String> lines;
    private List<Integer> bestTour;
    private int bestLength;
    /*Lưu trữ dữ liệu đầu vào, mỗi phần tử trong list là một dòng chứa thông tin về khoảng cách giữa các thành phố. */
    public BruteForce(List<String> lines) {
        this.lines = lines;
        this.bestTour = new ArrayList<>();
        this.bestLength = Integer.MAX_VALUE;
    }

    /*Tính toán độ dài cho mỗi hoán vị và cập nhật bestTour và bestLength nếu tìm thấy tour ngắn hơn.*/
    public List<Integer> solve() {
        long startTime = System.currentTimeMillis();
        int numCities = lines.size();
        List<Integer> tour = new ArrayList<>();
        for (int i = 1; i <= numCities; i++) {
            tour.add(i);
        }

        while (nextPermutation(tour)) {
            int currentLength = calculateTourLength(tour);
            if (currentLength < bestLength) {
                bestTour = new ArrayList<>(tour);
                bestLength = currentLength;
            }
        }

        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;
        System.out.println("\nBrute Force Runtime: " + executionTime + " seconds");
        return bestTour;
    }

    /*In ra thông tin của tour tốt nhất và độ dài của nó. */
    public void printTourAndLength() {
        System.out.println("Final Best Tour Brute: " + bestTour);
        System.out.println("Length of Best Tour Brute: " + bestLength);
    }

    /*Tạo ra hoán vị tiếp theo của danh sách đầu vào theo thứ tự từ điển.
    Được sử dụng để duyệt qua tất cả các cách đi có thể. */
    private boolean nextPermutation(List<Integer> array) {
        int i = array.size() - 1;
        while (i > 0 && array.get(i - 1) >= array.get(i)) {
            i--;
        }

        if (i <= 0) {
            return false;
        }

        int j = array.size() - 1;
        while (array.get(j) <= array.get(i - 1)) {
            j--;
        }

        // Swap the elements at indices i-1 and j
        Collections.swap(array, i - 1, j);

        // Reverse the suffix
        j = array.size() - 1;
        while (i < j) {
            Collections.swap(array, i, j);
            i++;
            j--;
        }

        return true;
    }

    /* Tính toán tổng chiều dài của tour dựa trên ma trận khoảng cách.
    Ma trận khoảng cách được xây dựng từ danh sách lines, với mỗi phần tử 
    là một chuỗi chứa khoảng cách từ một thành phố đến tất cả các thành phố khác.*/
    private int calculateTourLength(List<Integer> tour) {
        int[][] distanceMatrix = lines.stream()
                .map(line -> line.split(","))
                .map(arr -> {
                    int[] row = new int[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        row[i] = Integer.parseInt(arr[i]);
                    }
                    return row;
                })
                .toArray(int[][]::new);

        int totalLength = 0;
        for (int i = 0; i < tour.size(); i++) {
            totalLength += distanceMatrix[tour.get(i) - 1][tour.get((i + 1) % tour.size()) - 1];
        }
        return totalLength;
    }
}
