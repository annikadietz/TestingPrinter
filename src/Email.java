public class Email {
    String receiver;
    String mailContent;

    public Email(String pReceiver, String pContent) {
        receiver = pReceiver;
        mailContent = pContent;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
