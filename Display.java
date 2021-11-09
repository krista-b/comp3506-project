class DisplayRandom extends DisplayRandomBase {

    public DisplayRandom(String[] csvLines) {
        super(csvLines);
    }

    public void swap(Plane[] planes, int i, int j) {
        Plane temp = planes[i];
        planes[i] = planes[j];
        planes[j] = temp;
    }

    public int partition(Plane[] planes, int low, int high) {
        Plane pivot = planes[high];
        int i = (low - 1);

        for (int j = low; j < high; j++)
        {
            if (planes[j].compareTo(pivot) <= 0) {
                i++;
                swap(planes, i, j);
            }
        }
        swap(planes, i + 1, high);
        return (i + 1);
    }

    public void quickSort(Plane[] planes, int low, int high) {
        if (low < high) {
            int partitionIndex = partition(planes, low, high);

            quickSort(planes, low, partitionIndex - 1);
            quickSort(planes, partitionIndex + 1, high);
        }
    }

    @Override
    public Plane[] sort() {
        quickSort(this.getData(), 0, this.getData().length - 1);
        return this.getData();
    }
}

class DisplayPartiallySorted extends DisplayPartiallySortedBase {

    public DisplayPartiallySorted(String[] scheduleLines, String[] extraLines) {
        super(scheduleLines, extraLines);
    }

    void insertionSort(Plane[] planes) {
        int in, out;

        for (out = 1; out < planes.length; out++) {
            Plane temp = planes[out];
            in = out;

            while (in > 0 && planes[in - 1].compareTo(temp) >= 0) {
                planes[in] = planes[in - 1];
                --in;
            }
            planes[in] = temp;
        }
    }

    @Override
    Plane[] sort() {
        int scheduleLen = this.getSchedule().length;
        int extraLen = this.getExtraPlanes().length;

        Plane[] allPlanes =
                new Plane[scheduleLen + extraLen];
        System.arraycopy(this.getSchedule(), 0, allPlanes, 0,
                         scheduleLen);
        System.arraycopy(this.getExtraPlanes(), 0, allPlanes,
                         scheduleLen, extraLen);

        this.setSchedule(allPlanes);
        insertionSort(this.getSchedule());
        return this.getSchedule();
    }
}
