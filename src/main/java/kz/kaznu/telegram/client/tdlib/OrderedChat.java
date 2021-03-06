package kz.kaznu.telegram.client.tdlib;

public class OrderedChat implements Comparable<OrderedChat> {

    final long order;
    final long chatId;

    OrderedChat(long order, long chatId) {
        this.order = order;
        this.chatId = chatId;
    }

    @Override
    public int compareTo(OrderedChat o) {
        if (this.order != o.order) {
            return o.order < this.order ? -1 : 1;
        }
        if (this.chatId != o.chatId) {
            return o.chatId < this.chatId ? -1 : 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        OrderedChat o = (OrderedChat) obj;
        return this.order == o.order && this.chatId == o.chatId;
    }
}
