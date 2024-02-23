import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/*
 * TSPGeneticAlgorithm is an implementation of the GeneticAlgorithm interface for solving the Traveling Salesman Problem (TSP).
 * It provides methods for initializing a population, calculating the tour length, selecting parents, performing crossover and mutation,
 * and replacing the current population with a new population of offspring.
 */
public class TSPGeneticAlgorithm implements GeneticAlgorithm<List<Integer>> {
    private double crossoverProbability;
    private double mutationProbability;
    private List<String> lines;

    /**
     * Constructor to initialize TSPGeneticAlgorithm with crossover and mutation probabilities, and distance matrix lines.
     *
     * @param crossoverProbability The probability of crossover occurring during reproduction.
     * @param mutationProbability The probability of mutation occurring during reproduction.
     * @param lines A List of strings representing the distance matrix between cities.
     */
    public TSPGeneticAlgorithm(double crossoverProbability, double mutationProbability, List<String> lines) {
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        // this.crossoverProbability = 0.8;
        // this.mutationProbability = 0.1;
        this.lines = lines;
    }

    //* */ The initializePopulation method is used to create an initial population of individuals in the genetic algorithm
    @Override
    public List<List<Integer>> initializePopulation(int populationSize, int cities) {
        // Create a new list to store the initial population
        List<List<Integer>> population = new ArrayList<>();

        // Loop to generate individuals for the population
        for (int i = 0; i < populationSize; i++) {
            // Ensure uniqueness of individuals in the population
            while (true) {
                // Generate a random path for an individual
                List<Integer> path = getRandomPath(cities);

                // Check if the population already contains this path
                if (!population.contains(path)) {
                    // Add the unique path to the population
                    population.add(path);
                    break; // Exit the loop for this individual
                }
            }
        }

        // Return the initialized population
        return population;
    }

    //* */ Return the generated random path, which represents a random permutation of integers from 1 to the number of cities
    private List<Integer> getRandomPath(int cities) {
        // Create a new list to store the path
        List<Integer> path = new ArrayList<>();
    
        // Populate the path with integers from 1 to the number of cities
        for (int i = 1; i <= cities; i++) {
            path.add(i);
        }
    
        /*Shuffle the elements in the path to create a random permutation*/ 
        Collections.shuffle(path);
            return path;
    }
    
    //* */ Return the calculated total length of the tour. This represents the sum of distances between consecutive cities in the tour
    @Override
    public int tourLength(List<Integer> tour) {
        /*
         * Convert the lines of the distance matrix (read from the input file) into a 2D array (distanceMatrix). Each line represents distances from one city to all other cities.
         */ 
        int[][] distanceMatrix = lines.stream()
                .map(line -> Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray())
                .toArray(int[][]::new);

        /*
         *  For each city in the tour, add the distance from the current city to the next city to the totalLength. The distance is retrieved from the distanceMatrix.
         *  The modulo operation ((i + 1) % tour.size()) is used to handle the circular nature of the tour, ensuring that the last city is connected back to the first city.
         */
        int totalLength = 0;
        for (int i = 0; i < tour.size(); i++) {
            totalLength += distanceMatrix[tour.get(i) - 1][tour.get((i + 1) % tour.size()) - 1];
        }
        return totalLength;
    }


    @Override
    public List<Integer> selectParent(List<List<Integer>> population, List<Integer> fitnessValues) {
        // Calculate the total fitness of the population
        int totalFitness = fitnessValues.stream().mapToInt(Integer::intValue).sum();

        // Initialize a list to store cumulative probabilities for selection
        List<Double> cumulativeProbabilities = new ArrayList<>();
        double cumulativeProbability = 0.0;

        // Calculate cumulative probabilities for selection
        /*
         * Use a loop to calculate cumulative probabilities for each individual in the population.
         * The probability for each individual is its fitness divided by the total fitness
         */
        for (int i = 0; i < fitnessValues.size(); i++) {
            cumulativeProbability += (double) fitnessValues.get(i) / totalFitness;
            cumulativeProbabilities.add(cumulativeProbability);
        }

        // Generate a random value between 0 and 1
        double randomValue = Math.random();

        // Select a parent based on the random value and cumulative probabilities
        /*
         * Use the random value to select a parent based on cumulative probabilities.
         * The loop iterates through the cumulative probabilities until it finds the first one greater than the random value.
         * Print information (for debugging or analysis) about the selected parent's index and the cumulative probability.
         */
        for (int i = 0; i < cumulativeProbabilities.size(); i++) {
            if (randomValue <= cumulativeProbabilities.get(i)) {
                // Print information for debugging or analysis
                System.out.println("Selected parent index: " + i + ", Probability Distribution: " + cumulativeProbabilities.get(i));
                
                // Return the selected parent from the population
                return population.get(i);
            }
        }

        //* */ If no individual is selected based on the random value, return the last individual in the population as a fallback. This ensures that at least one individual is always selected
        return population.get(population.size() - 1);
    }

    //* */ Return the two child paths if crossover occurred, or return the parents if no crossover occurred
    @Override
    public List<List<Integer>> crossover(List<Integer> parent1, List<Integer> parent2) {
        //* */ Use the crossover probability to determine whether a crossover should occur. If the random value is less than or equal to the crossover probability, perform crossover; otherwise, return the parents without crossover
        if (Math.random() <= crossoverProbability) {
            // Randomly select a crossover point
            int crossoverPoint = new Random().nextInt(parent1.size() - 1) + 1;

            // Create two child paths using crossover
            List<Integer> child1 = new ArrayList<>(parent1.subList(0, crossoverPoint));
            List<Integer> child2 = new ArrayList<>(parent2.subList(0, crossoverPoint));

            List<List<Integer>> childRef = Arrays.asList(child1, child2);

            //* */ Identify the remaining cities for each child by filtering out the cities already present in the partial child paths
            List<Integer> remainingCitiesChild1 = parent2.stream()
                    .filter(city -> !childRef.get(0).contains(city))
                    .collect(Collectors.toList());
            List<Integer> remainingCitiesChild2 = parent1.stream()
                    .filter(city -> !childRef.get(1).contains(city))
                    .collect(Collectors.toList());

            //* */ Add the remaining cities to each child's path and use the fixDuplicates method to handle any duplicate cities
            child1.addAll(remainingCitiesChild1);
            child2.addAll(remainingCitiesChild2);

            childRef.set(0, fixDuplicates(child1, parent1));
            childRef.set(1, fixDuplicates(child2, parent2));

            return childRef;
        } else {
            // If no crossover, return the parents
            return Arrays.asList(parent1, parent2);
        }
    }

    //* */ After fixing duplicates, return the child path with the duplicates replaced by unused cities from the parent
    private List<Integer> fixDuplicates(List<Integer> child, List<Integer> parent) {
        // Fix any duplicates in the child path by replacing them with unused cities from the parent
        for (int i = 0; i < child.size(); i++) {
            /* Check if the current city in the child path has duplicates
             * Use Collections.frequency to check if the current city in the child path has duplicates
             * If duplicates are found, iterate through the parent path to find an unused city
             * If an unused city is found, replace the duplicate in the child path with the unused city
            */ 
            if (Collections.frequency(child, child.get(i)) > 1) {
                // Iterate through the parent path to find an unused city
                for (int city : parent) {
                    // If the city is not present in the child path, replace the duplicate with the unused city
                    if (!child.contains(city)) {
                        child.set(i, city);
                        break;
                    }
                }
            }
        }
        // Return the child path with fixed duplicates
        return child;
    }

    //* */ Return the mutated individual if mutation occurred, or return the original individual if no mutation occurred
    @Override
    public List<Integer> mutate(List<Integer> individual) {
        // Check if mutation should occur based on the mutation probability
        if (Math.random() <= mutationProbability) {
            // Randomly select two mutation points
            int mutationPoint1 = new Random().nextInt(individual.size());
            int mutationPoint2 = new Random().nextInt(individual.size());

            //* */ Perform mutation by swapping the values at the two randomly selected mutation points in the individual
            Collections.swap(individual, mutationPoint1, mutationPoint2);
        }
        // Return the mutated individual
        return individual;
    }

    /*
     * This method replaces the current population with a combination of individuals from both the current population and the offspring population. 
     * The replacement is based on the total tour lengths of the individuals, with the fittest individuals being selected to form the new population
     */
    @Override
    public List<List<Integer>> replacePopulation(List<List<Integer>> currentPopulation, List<List<Integer>> offspringPopulation) {
        //* */ Create a new list (combinedPopulation) by adding all individuals from the current population and offspring population. This step combines the solutions from both populations
        List<List<Integer>> combinedPopulation = new ArrayList<>(currentPopulation);
        combinedPopulation.addAll(offspringPopulation);

        /*
         * Sort the combined population based on tour lengths
         * Use Comparator.comparingInt to sort the combined population based on the total tour lengths of the individuals. The tourLength method is used as the key for comparison
         */
        combinedPopulation.sort(Comparator.comparingInt(this::tourLength));

        /* 
        *
        */ 
        return combinedPopulation.subList(0, currentPopulation.size());
    }
}




/*
 * Khởi tạo (phương thức initializePopulation):

Một quần thể các cá thể được tạo ra, mỗi cá thể đại diện cho một giải pháp có thể cho TSP.
Kích thước quần thể được xác định bởi tham số populationSize.
Đối với mỗi cá thể, một đường đi ngẫu nhiên (hoán vị của các thành phố) được tạo và thêm vào quần thể.
Đảm bảo tính duy nhất của các cá thể.
Độ phức tạp thời gian: O(populationSize * cities^2) - Tạo một đường đi ngẫu nhiên liên quan đến việc trộn một danh sách các thành phố, mất O(cities^2) thời gian.

Tính độ dài hành trình (phương thức tourLength):

Tính tổng độ dài của một hành trình cụ thể bằng cách cộng tổng các khoảng cách giữa các thành phố liên tiếp.
Ma trận khoảng cách được xây dựng từ các dòng đầu vào.
Độ phức tạp thời gian: O(cities^2) - Tính độ dài hành trình liên quan đến việc truy cập khoảng cách trong ma trận cho mỗi cặp thành phố liên tiếp.

Chọn cha mẹ (phương thức selectParent):

Sử dụng phương pháp lựa chọn bằng cách quay xe roulette dựa trên giá trị thích nghi (độ dài hành trình).
Tính xác suất tích lũy cho mỗi cá thể trong quần thể.
Một giá trị ngẫu nhiên được tạo để chọn cha mẹ dựa trên xác suất này.
Độ phức tạp thời gian: O(populationSize) - Tính toán xác suất tích lũy liên quan đến việc lặp qua quần thể.

Crossover (phương thức crossover):

Xác định xem có thực hiện crossover dựa trên xác suất crossover hay không.
Nếu có crossover, tạo hai đường đi con bằng cách kết hợp các phần của đường đi cha mẹ.
Các thành phố còn lại được thêm vào đường đi của mỗi đứa con, và các trùng lặp được sửa chữa.
Độ phức tạp thời gian: O(cities) - Tạo đường đi con liên quan đến các thao tác sao chép và lọc.

Mutation (phương thức mutate):

Xác định xem có thực hiện đột biến dựa trên xác suất đột biến.
Nếu đột biến xảy ra, hai vị trí ngẫu nhiên trong đường đi của cá thể được hoán đổi.
Độ phức tạp thời gian: O(1) - Hoán đổi hai phần tử trong đường đi.

Thay thế quần thể (phương thức replacePopulation):

Kết hợp quần thể hiện tại và quần thể con cái.
Sắp xếp quần thể kết hợp dựa trên độ dài hành trình.
Chọn các cá thể hàng đầu để tạo ra quần thể mới.
Độ phức tạp thời gian: O((currentPopulation + offspringPopulation) * log(currentPopulation + offspringPopulation)) - Sắp xếp quần thể kết hợp.

Tổng Độ phức tạp thời gian:

Vòng lặp chính của thuật toán di truyền bao gồm các bước khởi tạo, đánh giá, lựa chọn, crossover, mutation và thay thế. Gọi N là số thế hệ.
Độ phức tạp thời gian tổng cộng là khoảng O(N * populationSize * cities^2).
Độ phức tạp Không Gian:

Độ phức tạp không gian nổi bật nhất là lưu trữ các quần thể và ma trận.
Nó là O(populationSize * cities) cho mỗi thế hệ.
 * 
 */