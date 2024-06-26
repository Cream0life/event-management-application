import React, { createContext, useState, useContext, useEffect } from 'react';
import Cookies from 'js-cookie';
import { Toast } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [loggedInUser, setLoggedInUser] = useState(null);
    const [error, setError] = useState(null);
    const [showLogoutToast, setShowLogoutToast] = useState(false);
    const navigate = useNavigate();

    const handleLogin = (user) => {
        setLoggedInUser(user);
        Cookies.set('loggedInUser', JSON.stringify(user), { expires: 7 });
    };

    const handleLogout = () => {
        setLoggedInUser(null);
        Cookies.remove('loggedInUser');
        setShowLogoutToast(true);
        navigate('/');
    };

    useEffect(() => {
        const storedUser = Cookies.get('loggedInUser');
        if (storedUser) {
            setLoggedInUser(JSON.parse(storedUser));
        }
    }, []);

    return (
        <UserContext.Provider value={{ loggedInUser, error, handleLogin, handleLogout, setError }}>
            {children}
            {/* Logout Success Toast */}
            <Toast
                onClose={() => setShowLogoutToast(false)}
                show={showLogoutToast}
                delay={3000}
                autohide
                bg="success"
                style={{
                    position: 'fixed',
                    top: 20,
                    right: 20,
                }}
            >
                <Toast.Body>Logged out successfully!</Toast.Body>
            </Toast>
        </UserContext.Provider>
    );
};

export const useUser = () => useContext(UserContext);
