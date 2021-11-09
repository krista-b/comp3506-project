public class Dispatcher extends DispatcherBase {

    public static class Node {
        Plane currentPlane;
        Node previous;
        Node next;

        public Node(Plane plane) {
            this.currentPlane = plane;
        }
    }

    public Node head = null;

    @Override
    public int size() {
        int count = 0;
        Node temp = head;
        while (temp != null) {
            count++;
            temp = temp.next;
        }
        return count;
    }

    @Override
    public void addPlane(String planeNumber, String time) {
        Plane plane = new Plane(planeNumber, time);

        Node currentNode;
        Node newNode = new Node(plane);

        if (head == null) {
            head = newNode;
        } else if (head.currentPlane.compareTo(newNode.currentPlane) > 0) {
            newNode.next = head;
            newNode.next.previous = newNode;
            head = newNode;
        } else {
            currentNode = head;

            while (currentNode.next != null &&
                    currentNode.next.currentPlane.compareTo(newNode.currentPlane) < 0) {
                currentNode = currentNode.next;
            }

            newNode.next = currentNode.next;

            if (currentNode.next != null) {
                newNode.next.previous = newNode;
            }

            currentNode.next = newNode;
            newNode.previous = currentNode;
        }
    }

    public int getIntTime(String time) {
        StringBuilder sb = new StringBuilder(time);
        sb.deleteCharAt(2);
        String resultString = sb.toString();
        return Integer.parseInt(resultString);
    }

    @Override
    public String allocateLandingSlot(String currentTime) {
        if (head == null) {
            return null;
        }
        int firstPlaneTime = head.currentPlane.getIntTime();
        int currentIntTime = getIntTime(currentTime);
        if (firstPlaneTime > currentIntTime + 5) {
            return null;
        }
        String allocated = head.currentPlane.getPlaneNumber();
        head = head.next;
        return allocated;
    }

    @Override
    public String emergencyLanding(String planeNumber) {

        if (!isPresent(planeNumber)) {
            return null;
        }

        Node temp = head;

        while (temp != null) {
            if (temp.currentPlane.getPlaneNumber().equals(planeNumber)) {
                if(temp == head) {
                    head = head.next;
                    temp = head;
                } else {
                    temp.previous.next = temp.next;
                    temp = temp.next;
                }
            } else {
                temp.previous = temp;
                temp = temp.next;
            }
        }
        return planeNumber;
    }

    @Override
    public boolean isPresent(String planeNumber) {
        boolean flag = false;
        Node currentNode = head;

        if(head == null) {
            return false;
        }

        while(currentNode != null) {
            if(currentNode.currentPlane.getPlaneNumber().equals(planeNumber)) {
                flag = true;
                break;
            }
            currentNode = currentNode.next;
        }
        return flag;
    }
}

