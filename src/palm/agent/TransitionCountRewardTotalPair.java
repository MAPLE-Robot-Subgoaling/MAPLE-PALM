//package ramdp.agent;
//
//public class TransitionCountRewardTotalPair {
//
//
//    public TransitionCountRewardTotalPair(int transitionCount, double rewardTotal) {
//        this.transitionCount = transitionCount;
//        this.rewardTotal = rewardTotal;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        TransitionCountRewardTotalPair that = (TransitionCountRewardTotalPair) o;
//
//        if (transitionCount != that.transitionCount) return false;
//        return Double.compare(that.rewardTotal, rewardTotal) == 0;
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        long temp;
//        result = transitionCount;
//        temp = Double.doubleToLongBits(rewardTotal);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "TransitionCountRewardTotalPair{" +
//                "transitionCount=" + transitionCount +
//                ", rewardTotal=" + rewardTotal +
//                '}';
//    }
//}
