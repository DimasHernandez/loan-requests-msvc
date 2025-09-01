package co.com.pragma.consumer.user.errorclient;

public record ErrorClient(

        String error,

        String status,

        String detail
) {
    public ErrorClient(String error, String status, String detail) {
        this.error = error;
        this.status = status;
        this.detail = detail;
    }
}
