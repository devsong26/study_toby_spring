package user.domain;

public class Message {
    String text;

    private Message(String text){
        this.text = text; //생성자가 private 으로 선언되어 있어서 외부에서 생성자를 통해 오브젝트를 만들 수 없다.
    }

    public String getText(){
        return text;
    }

    public static Message newMessage(String text){
        return new Message(text); // 생성자 대신 사용할 수 있는 스태틱 팩토리 메서드를 제공한다.
    }

}
