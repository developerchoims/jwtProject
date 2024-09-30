
import api from "../main/axios/axiosApi";
import PortOne from "@portone/browser-sdk/v2";

const IamPort = ({ cartdetail }) => {
    const handlePayment = async () => {
        const orderName = cartdetail.length === 1 ? cartdetail[0].name : `${cartdetail[0].name} 외 ${cartdetail.length - 1}건`;
        const totalAmount = cartdetail.reduce((total, p) => total + p.cartAmount, 0);
        const paymentId = crypto.randomUUID();
        const method = "CARD";

        try {
            const response = await PortOne.requestPayment({
                storeId: "store-71704625-36a0-46e1-bdbd-00da604507ef",
                channelKey: "channel-key-237352d9-f99e-44e9-80fe-8b5a016a0581",
                paymentId: `payment-${paymentId}`,
                orderName,
                totalAmount,
                currency: "CURRENCY_KRW",
                payMethod: method,
                redirectUrl: `https://hoopi.co.kr/order-redirect`
        });

            if (response.code != null) {
                alert(response.message);
                return;
            }

            let productCodes = cartdetail.map(p => p.productCode);
            let paymentAmount = cartdetail.reduce((sum, p) => sum + p.paymentAmount, 0);

            const notified = await api.post(`hoopi/order`, {
                cartCode: cartdetail[0].cartCode,
                productCode: productCodes.join(','),
                paymentCode: paymentId,
                method,
                bank: '나이스페이먼츠',
                paymentAmount : 0
            });

            alert(notified.data);
        } catch (error) {
            console.error(error);
            alert("결제 처리 중 오류가 발생했습니다.");
        }
    };

    return (
        <div>
            <button onClick={handlePayment}>결제</button>
        </div>
    );
};

export default IamPort;