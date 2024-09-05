import React, { useContext, createContext, useState, useEffect } from 'react';
import axios from 'axios';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import Login from './Login';
import Join from './Join';
import './App.css';
import Main from "./main/Main";
import Search from "./search/Search";
import './main/axios/axiosInterceptor.js';

// Context 생성
const UserContext = createContext(null);

// App 컴포넌트
function App() {

    const handleLogout = () => {
        const id = localStorage.getItem('id');
        axios.delete('http://hoopi.p-e.kr/api/hoopi/logout', {params: {id}})
            .then(response => {
                localStorage.removeItem('id');
                localStorage.removeItem('role');
                alert(response.data);
                window.location.reload('/');
            })
            .catch(error => {
                alert(error.response.data)
            });
    }
    return (
            <Router>
                <div className='mainNav-container'>
                    <div className='mainNav-sns-box'>

                    </div>
                    <div className='mainNav-img-box'>
                        <Link to='/'><img src='/nata_logo.png' /></Link>
                    </div>
                    <div className='mainNav-link-box'>
                        <Link to="/join">회원가입</Link>
                        <Link to="/login">로그인</Link>
                    </div>
                </div>
                <Search/>
                <div>
                    <Routes>
                        <Route path='/' element={<Main/>}/>
                        <Route path='/join' element={<Join />}/>
                        <Route path='/login' element={<Login />}/>
                    </Routes>
                </div>
            </Router>
    );
}

export default App;
export {UserContext};