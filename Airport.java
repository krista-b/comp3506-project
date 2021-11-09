import java.util.*;
import java.util.stream.Collectors;

public class Airport extends AirportBase {

    private HashMap<TerminalBase, LinkedList<ShuttleBase>> adjList;
    private HashMap<TerminalBase, Integer> indexes;
    private int index;
    private HashMap<TerminalBase, Integer> distances;
    private HashSet<TerminalBase> visited;
    private ArrayList<TerminalBase> inPath;
    private PriorityQueue<Node> pq;

    /**
     * Creates a new AirportBase instance with the given capacity.
     *
     * @param capacity capacity of the airport shuttles
     *                 (same for all shuttles)
     */
    public Airport(int capacity) {
        super(capacity);

        adjList = new HashMap<>();
        index = 0;
        indexes = new HashMap<>();
        pq = new PriorityQueue<>(new Node());
    }

    @Override
    public TerminalBase opposite(ShuttleBase shuttle, TerminalBase terminal) {
        if (shuttle.getOrigin().getId().equals(terminal.getId()))
            return shuttle.getDestination();
        if (shuttle.getDestination().getId().equals(terminal.getId()))
            return shuttle.getOrigin();

        return null;
    }

    @Override
    public TerminalBase insertTerminal(TerminalBase terminal) {
        LinkedList<ShuttleBase> list = new LinkedList<>();
        adjList.put(terminal, list);
        indexes.put(terminal, index++);
        return terminal;
    }

    @Override
    public ShuttleBase insertShuttle(TerminalBase origin, TerminalBase destination, int time) {
        ShuttleBase shuttle = new Shuttle(origin, destination, time);
        LinkedList<ShuttleBase> list;
        list = adjList.get(origin);
        list.addFirst(shuttle);
        adjList.put(origin, list);
        list = adjList.get(destination);
        list.addFirst(shuttle);
        adjList.put(destination, list);
        return shuttle;
    }

    @Override
    public boolean removeTerminal(TerminalBase terminal) {
        if (indexes.get(terminal) == null)
            return false;

        List<ShuttleBase> toRemove = new ArrayList<>();
        for (TerminalBase term : adjList.keySet()) {
            for (ShuttleBase shuttle : adjList.get(term))
                if (shuttle.getOrigin() == terminal)
                    toRemove.add(shuttle);
                else if (shuttle.getDestination() == terminal)
                    toRemove.add(shuttle);
            adjList.get(term).removeAll(toRemove);
        }
        adjList.remove(terminal);
        indexes.remove(terminal);
        index--;

        return true;
    }

    @Override
    public boolean removeShuttle(ShuttleBase shuttle) {
        List<ShuttleBase> toRemove = new ArrayList<>();
        for (TerminalBase term : adjList.keySet()) {
            for (ShuttleBase sh : adjList.get(term))
                if (sh == shuttle)
                    toRemove.add(sh);
            adjList.get(term).removeAll(toRemove);
        }
        return true;
    }

    @Override
    public List<ShuttleBase> outgoingShuttles(TerminalBase terminal) {
        return this.adjList.get(terminal);
    }

    @Override
    public Path findShortestPath(TerminalBase origin, TerminalBase destination) {
        distances = new HashMap<>();
        visited = new HashSet<>();
        inPath = new ArrayList<>();
        Queue<TerminalBase> queue = new ArrayDeque<>();

        for (TerminalBase term : adjList.keySet())
            distances.put(term, Integer.MAX_VALUE);

        distances.replace(origin, 0);
        queue.add(origin);

        while (queue.size() != 0) {
            TerminalBase vertex = queue.poll();
            if (!visited.contains(vertex)) {
                inPath.add(vertex);
                if (vertex.equals(destination))
                    break;
                queue.addAll(getNeighbours(vertex));
                visited.add(vertex);
            }
        }

        return new Path(inPath, distances.get(destination));
    }

    /**
     * Helper function to get all adjacent terminals to the given terminal,
     * i.e. has an adjoining shuttle.
     *
     * @param vertex terminal of which neighbours are to be found
     * @return list of adjacent terminals
     */
    private List<TerminalBase> getNeighbours(TerminalBase vertex) {
        List<TerminalBase> neighbours = new ArrayList<>();
        for (ShuttleBase shuttle : adjList.get(vertex)) {
            if (shuttle.getOrigin().equals(vertex)) {
                if (!neighbours.contains(shuttle.getDestination())) {
                    neighbours.add(shuttle.getDestination());
                    distances.replace(shuttle.getDestination(), shuttle.getTime() + shuttle.getOrigin().getWaitingTime());
                }
            } else {
                if (!neighbours.contains(shuttle.getOrigin())) {
                    neighbours.add(shuttle.getOrigin());
                    distances.replace(shuttle.getOrigin(), shuttle.getTime() + shuttle.getDestination().getWaitingTime());
                }
            }
        }
        return neighbours;
    }

    @Override
    public Path findFastestPath(TerminalBase origin, TerminalBase destination) {
        distances = new HashMap<>();
        visited = new HashSet<>();
        inPath = new ArrayList<>();

        for (TerminalBase term : adjList.keySet()) {
            distances.put(term, Integer.MAX_VALUE);
        }

        pq.add(new Node(origin, 0));
        distances.replace(origin, 0);

        while (visited.size() != index) {
            if (pq.isEmpty())
                return null;

            TerminalBase term = pq.remove().terminal;

            if (visited.contains(term))
                continue;

            visited.add(term);
            processNeighboursFastest(term);
        }
        inPath.add(destination);

        return new Path(inPath, distances.get(destination));
    }

    /**
     * Helper function to visit all adjacent terminals to the given terminal
     * and compare the path distances between them.
     *
     * @param term terminal of which its neighbours are to be processed
     */
    private void processNeighboursFastest(TerminalBase term) {
        int edgeDist;
        int newDist;

        for (ShuttleBase shuttle : outgoingShuttles(term)) {
            Node neighbour;
            if (shuttle.getOrigin().equals(term))
                neighbour = new Node(shuttle.getDestination(), shuttle.getTime() + shuttle.getOrigin().getWaitingTime());
            else
                neighbour = new Node(shuttle.getOrigin(), shuttle.getTime() + shuttle.getDestination().getWaitingTime());

            if (!visited.contains(neighbour.terminal)) {
                edgeDist = neighbour.cost;
                newDist = distances.get(term) + edgeDist;

                if (newDist < distances.get(neighbour.terminal)) {
                    distances.replace(neighbour.terminal, newDist);
                    inPath.remove(term);
                    inPath.add(term);
                }

                pq.add(new Node(neighbour.terminal,
                                distances.get(neighbour.terminal)));
            }
        }
    }
    
    static class Terminal extends TerminalBase {

        /**
         * Creates a new TerminalBase instance with the given terminal ID
         * and waiting time.
         *
         * @param id          terminal ID
         * @param waitingTime waiting time for the terminal, in minutes
         */
        public Terminal(String id, int waitingTime) {
            super(id, waitingTime);
        }
    }

    static class Shuttle extends ShuttleBase {
        /**
         * Creates a new ShuttleBase instance, travelling from origin to
         * destination and requiring 'time' minutes to travel.
         *
         * @param origin      origin terminal
         * @param destination destination terminal
         * @param time        time required to travel, in minutes
         */
        public Shuttle(TerminalBase origin, TerminalBase destination, int time) {
            super(origin, destination, time);
        }
    }

    public static void main(String[] args) {
        Airport a = new Airport(3);
        Terminal terminalA = (Terminal) a.insertTerminal(new Terminal("A", 1));
        Terminal terminalB = (Terminal) a.insertTerminal(new Terminal("B", 3));
        Terminal terminalC = (Terminal) a.insertTerminal(new Terminal("C", 4));
        Terminal terminalD = (Terminal) a.insertTerminal(new Terminal("D", 2));

        Shuttle shuttle1 = (Shuttle) a.insertShuttle(terminalA, terminalB, 2);
        Shuttle shuttle2 = (Shuttle) a.insertShuttle(terminalA, terminalC, 5);
        Shuttle shuttle3 = (Shuttle) a.insertShuttle(terminalA, terminalD, 18);
        Shuttle shuttle4 = (Shuttle) a.insertShuttle(terminalB, terminalD, 8);
        Shuttle shuttle5 = (Shuttle) a.insertShuttle(terminalC, terminalD, 15);

        // Opposite
        assert a.opposite(shuttle1, terminalA).getId().equals("B");

        // Outgoing Shuttles
        assert a.outgoingShuttles(terminalA).stream()
                .map(ShuttleBase::getTime)
                .collect(Collectors.toList()).containsAll(List.of(2, 5, 18));


        // Remove Terminal
        //        Terminal terminalTest = new Terminal("E", 12);
        //        System.out.println(a.removeTerminal(terminalTest));
        a.removeTerminal(terminalC);
        assert a.outgoingShuttles(terminalA).stream()
                .map(ShuttleBase::getTime)
                .collect(Collectors.toList()).containsAll(List.of(2, 18));

        // Shortest path
        Path shortestPath = a.findShortestPath(terminalA, terminalD);
        System.out.println(shortestPath.terminals);
        System.out.println(shortestPath.time);
        assert shortestPath.terminals.stream()
                .map(TerminalBase::getId)
                .collect(Collectors.toList()).equals(List.of("A", "D"));
        assert shortestPath.time == 19;

        // Fastest path
        Path fastestPath = a.findFastestPath(terminalA, terminalD);
        System.out.println(fastestPath.terminals);
        System.out.println(fastestPath.time);

        assert fastestPath.terminals.stream()
                .map(TerminalBase::getId)
                .collect(Collectors.toList()).equals(List.of("A", "B", "D"));


        assert fastestPath.time == 14;

        Airport b = new Airport(6);
        Terminal A = (Terminal) b.insertTerminal(new Terminal("A_", 1));
        Terminal B = (Terminal) b.insertTerminal(new Terminal("B_", 2));
        Terminal C = (Terminal) b.insertTerminal(new Terminal("C_", 4));
        Terminal D = (Terminal) b.insertTerminal(new Terminal("D_", 2));

        Shuttle s1 = (Shuttle) b.insertShuttle(A, B, 3);
        Shuttle s2 = (Shuttle) b.insertShuttle(A, D, 1);
        Shuttle s3 = (Shuttle) b.insertShuttle(A, C, 3);
        Shuttle s4 = (Shuttle) b.insertShuttle(B, D, 4);
        Shuttle s5 = (Shuttle) b.insertShuttle(C, D, 2);

        Path path = b.findFastestPath(B, C);
        System.out.println(path.terminals);
        assert path.terminals.stream()
                .map(TerminalBase::getId)
                .collect(Collectors.toList()).equals(List.of("B_", "A_", "C_"));
        System.out.println(path.time);
        assert path.time == 9;

        Airport c = new Airport(6);
        Terminal Ac = (Terminal) c.insertTerminal(new Terminal("A_c", 1));
        Terminal Bc = (Terminal) c.insertTerminal(new Terminal("B_c", 10));
        Terminal Cc = (Terminal) c.insertTerminal(new Terminal("C_c", 2));
        Terminal Dc = (Terminal) c.insertTerminal(new Terminal("D_c", 2));

        Shuttle s4_c = (Shuttle) c.insertShuttle(Ac, Cc, 10);
        Shuttle s1_c = (Shuttle) c.insertShuttle(Ac, Bc, 2);
        Shuttle s2_c = (Shuttle) c.insertShuttle(Bc, Dc, 8);
        Shuttle s3_c = (Shuttle) c.insertShuttle(Cc, Dc, 8);

        Path pathC = c.findFastestPath(Ac, Dc);
        System.out.println(pathC.terminals);
    }
}

class Node implements Comparator<Node> {

    AirportBase.TerminalBase terminal;
    int cost;

    /**
     * Creates a new Node instance.
     */
    public Node() {}

    /**
     * Creates a new Node instance with the given terminal and associated
     * terminal cost.
     *
     * @param terminal airport terminal
     * @param cost cost of terminal (terminal waiting time + outgoing shuttle
     *             travel time)
     */
    public Node(AirportBase.TerminalBase terminal, int cost) {
        this.terminal = terminal;
        this.cost = cost;
    }

    @Override
    public int compare(Node n1, Node n2) {
        return Integer.compare(n1.cost, n2.cost);
    }
}