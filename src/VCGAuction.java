import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCGAuction {

    private Map<Integer, List<Integer>> bidMapItem;
    private List<BidInfo> bidMaxWelfareList;
    private List<BidInfo> bidCurWelfareList;

    private int numItems, numBidders, maxWelfare, curWelfare;

    private VCGAuction(int[][] info, int numItems, int numBidders) {
        this.numItems = numItems;
        this.numBidders = numBidders;
        this.maxWelfare = this.curWelfare = 0;

        this.bidMaxWelfareList = new ArrayList<>();
        this.bidCurWelfareList = new ArrayList<>();

        bidMapItem = new HashMap<>();
        for (int i = 0; i < numItems; i++) {
            List<Integer> itemValues = new ArrayList<>();
            for (int j = 0; j < numBidders; j++) {
                itemValues.add(info[j][i]);
            }
            bidMapItem.put(i, itemValues);
        }
    }

    private void auction() {
        for (int i = 0; i < numBidders; i++) {
            findMaxWelfare(null, i, 0);
        }
        System.out.println("Allocations:");
        for (int i = 0; i < numItems; i++) {
            BidInfo bidInfo = bidMaxWelfareList.get(i);
            System.out.println(bidInfo.toString());
        }
        System.out.println("Bidder: payment = maxWelfare - curWelfare");
        for (BidInfo bidInfo: bidMaxWelfareList) {
            for(int i = 0; i<numBidders; i++){
                if(i != bidInfo.bidder)
                    findCurWelfare(null, i, 0, bidInfo.bidder);
            }
                int payment = maxWelfare - curWelfare;
                for (int i = 0; i < numItems; i++) {
                    BidInfo bidInfo2 = bidCurWelfareList.get(i);
                System.out.println(bidInfo2.toString());
            }
            System.out.println("Bidder:" + (bidInfo.bidder + 1) + ": " + payment + "=" + maxWelfare + "-" + curWelfare);
        }
    }

    private BidTree findMaxWelfare(List<BidTree> childList, int bidder, int item) {
        BidTree root = new BidTree();
        if (childList == null) {
            BidInfo bidInfo = new BidInfo(bidder, bidMapItem.get(item).get(bidder), item);
            root = new BidTree(bidInfo);

            addChildHelper(item + 1, root, -1);
            findMaxWelfare(root.children, bidder, item + 1);
        } else if (item < numItems - 1) {
            for (int i = 0; i < childList.size(); i++) {
                root = childList.get(i);
                addChildHelper(item + 1, root, -1);
                findMaxWelfare(root.children, bidder, item + 1);
            }
        } else {
            for (int i = 0; i < childList.size(); i++) {
                root = childList.get(i);
                int current = root.totalWelfare();
                if (maxWelfare < current) {
                    maxWelfare = current;
                    bidMaxWelfareList = new ArrayList<>();
                    while (root != null) {
                        bidMaxWelfareList.add(root.data);
                        root = root.parent;
                    }
                }
            }
        }
        return root;
    }

    private void addChildHelper(int item, BidTree root, int exclude) {
        BidInfo bidInfo;
        if (item < numItems) {
            for (int child_j = 0; child_j < numBidders; child_j++) {
                if (!root.isParent(child_j, root) && root.data.bidder != child_j && child_j != exclude) {
                    bidInfo = new BidInfo(child_j, bidMapItem.get(item).get(child_j), item);
                    root.addChild(bidInfo);
                }
            }
        }
    }


    private BidTree findCurWelfare(List<BidTree> childList, int bidder, int item, int exclude) {
        BidTree root = new BidTree();
        if (childList == null) {
            BidInfo bidInfo = new BidInfo(bidder, bidMapItem.get(item).get(bidder), item);
            root = new BidTree(bidInfo);

            addChildHelper(item + 1, root, exclude);
            findCurWelfare(root.children, bidder, item + 1, exclude);
        } else if (item < numItems - 1) {
            for (int i = 0; i < childList.size(); i++) {
                root = childList.get(i);
                addChildHelper(item + 1, root, exclude);
                findCurWelfare(root.children, bidder, item + 1, exclude);
            }
        } else {
            for (int i = 0; i < childList.size(); i++) {
                root = childList.get(i);
                int current = root.totalWelfare();
                if (curWelfare < current) {
                    curWelfare = current;
                    bidCurWelfareList = new ArrayList<>();
                    while (root != null) {
                        bidCurWelfareList.add(root.data);
                        root = root.parent;
                    }
                }
            }
        }
        return root;
    }

    public static void main(String[] args) throws Exception {
        File f = new File("C:\\Users\\Shilei Lin\\IdeaProjects\\VCG\\src\\bidder.mat");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(f));

        String line = bufferedReader.readLine();

        int numOfItems = Integer.parseInt(line.split(" ")[0]);
        int numOfBidders = Integer.parseInt(line.split(" ")[1]);

        int[][] info = new int[numOfBidders][numOfItems];
        int index = 0;

        while ((line = bufferedReader.readLine()) != null) {
            String[] values = line.split(" ");
            for (int j = 0; j < numOfItems; j++) {
                info[index][j] = Integer.valueOf(values[j]);
            }
            index++;
        }

        VCGAuction vcg = new VCGAuction(info, numOfItems, numOfBidders);
        vcg.auction();
    }

    private class BidInfo {
        int bidder;
        int value;
        int item;


        private BidInfo(int bidder, int value, int item) {
            this.bidder = bidder;
            this.value = value;
            this.item = item;
        }

        public int getBidder() {
            return bidder;
        }

        public void setBidder(int bidder) {
            this.bidder = bidder;
        }

        public String toString() {
            return "item:" + (this.item + 1) + " bidder:" + (this.bidder + 1) + " value:" + this.value;
        }
    }

    private class BidTree {

        BidInfo data;
        BidTree parent;
        List<BidTree> children;

        private BidTree() {
            this.data = null;
            this.children = new ArrayList<>();
        }

        private BidTree(BidInfo data) {
            this.data = data;
            this.children = new ArrayList<>();
        }

        private BidTree addChild(BidInfo child) {
            BidTree childNode = new BidTree(child);
            childNode.parent = this;
            this.children.add(childNode);
            return childNode;
        }

        private boolean isParent(int i, BidTree child) {
            if (child.parent == null)
                return false;
            else if (child.parent.data.bidder == i)
                return true;
            else {
                return isParent(i, child.parent);
            }
        }

        private int totalWelfare() {
            if (this.parent == null) {
                //System.out.print(data.value);
                return data.value;
            } else {
                //System.out.print(data.value + " + ");
                return data.value + parent.totalWelfare();
            }
        }

    }

}
