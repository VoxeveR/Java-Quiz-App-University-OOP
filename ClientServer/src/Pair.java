public class Pair<fT, sT> {
    private fT firstItem;
    private sT secondItem;

    public Pair(fT firstItem, sT secondItem){
        this.firstItem = firstItem;
        this.secondItem = secondItem;
    }

    public fT getFirst(){
        return firstItem;
    }

    public sT getSecond(){
        return secondItem;
    }

    public void setFirst(fT newFirst){
        this.firstItem = newFirst;
    }

    public void setSecond(sT newSecond){
        this.secondItem = newSecond;
    }
}
