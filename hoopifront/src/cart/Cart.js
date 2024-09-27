import {useEffect, useState} from "react";
import axios from "axios";
import './cart.css';
import $ from 'jquery';

const Cart = () => {

    const id = localStorage.getItem("id");
    const [cartdetail, setCartdetail] = useState([]);

    useEffect(() => {
        fetchCart();
    }, [])

    const fetchCart = async () => {
        try{
            const response = await axios.get(`http://hoopi.p-e.kr/api/hoopi/cart`,{params:{id:id}});
            console.log(response.data);
            setCartdetail(response.data);
        }catch(e){
            console.log(e);
        }
    }

    // 수량 변경, 가격 변경 시 DB 수정
    const handleUpdate = (e, productCode, quantity, cartAmount) => {
        const newQuantity = handleQuantityChange(e.target.value);
        const rawPrice = cartAmount/quantity;
        axios.put(`http://hoopi.p-e.kr/api/hoopi/cart`, {
            cartCode: cartdetail[0].cartCode
            , productCode: productCode
            , quantity: newQuantity
            , cartAmount: rawPrice * newQuantity})
            .then(response => {
                console.log(response.data);
                fetchCart();
            })
            .catch(error => {
                console.log(error);
            });
    }

    // 0보다 작아지는 걸 막기
    function handleQuantityChange(quantity) {
        const minValue = 1;
        if (quantity < minValue) {
            return minValue;
        } else {
            return quantity;
        }
    }
    // checkBox 전체 선택
    const handleSelectAll = (event) => {
        const isChecked = event.target.checked;
        $(".cart-checkbox").each(function() {
            $(this).prop("checked", isChecked);
        });
    };

    // 선택된 체크 박스의 아이디 불러오기
    const [selectedIds, setSelectedIds] = useState([]);
    const handleSelectPart = (event) => {
        console.log("event확인",event);
        const { id, checked } = event.target;
        if (checked) {
            // 체크된 경우, id 추가
            setSelectedIds(prev => [...prev, id]);
        } else {
            // 체크 해제된 경우, id 제거
            setSelectedIds(prev => prev.filter(x => x !== id));
        }
    };

    // 상품 부분 삭제 시 DB 수정
    const handleDeletePart = () => {
        console.log("selectedIds확인",selectedIds);
        axios.delete('http://hoopi.p-e.kr/api/hoopi/cart-part', {params:{
            cartCode: cartdetail[0].cartCode,
            productCodes: selectedIds.join(',')
            }})
            .then(response => {
                alert(response.data);
                fetchCart();
            })
            .catch(error=>{
                console.log(error);
            });
    }

    // 상품 전체 삭제 시 DB 수정
    const handleDeleteAll = () => {
        axios.delete('http://hoopi.p-e.kr/api/hoopi/cart-all', {params:{cartCode: cartdetail[0].cartCode}})
            .then(response =>{
                alert(response.data);
                fetchCart();
            })
            .catch(error=>{
                console.log(error);
            });
    }


    return(
        <div className="cart-container">
            <div className="cart-box">
                <table>
                    <thead>
                    <tr>
                        <th><input type="checkbox" className="cart-checkbox" onClick={handleSelectAll}/></th>
                        <th>
                            <button onClick={handleDeletePart}>선택 삭제</button>
                            <button onClick={handleDeleteAll}>전체 삭제</button>
                        </th>
                        <th>수량</th>
                        <th>가격</th>
                        <th>
                            <button>선택 주문</button>
                            <button>전체 주문</button>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {
                        !Array.isArray(cartdetail) || cartdetail.length === 0 ?
                            <tr>
                                <td colSpan={5}>"장바구니에 담긴 상품이 없습니다."</td>
                            </tr>
                        :cartdetail?.map((product, index) => (
                            <tr key={product.productCode}>
                                <td><input type="checkbox" className="cart-checkbox" id={product.productCode} onChange={handleSelectPart}/></td>
                                <td><img src={product.imgUrl} alt={product.imgUrl}/></td>
                                <td>
                                    <input type='number' value={product.quantity} min='1'
                                    onChange={(e) => handleUpdate(e, product.productCode, product.quantity, product.cartAmount)}/>
                                </td>
                                <td><p defaultValue={product.cartAmount}>{product.cartAmount}</p></td>
                                <td>
                                    <button>주문</button>
                                </td>
                            </tr>
                        ))
                    }
                    </tbody>
                </table>
            </div>
        </div>
    );
}
export default Cart;