import React, { useState, useEffect } from 'react';
import { Fade, Typography } from '@mui/material';

const phrases = ['Welcome to the Anime Recommender','Never have a bad anime pick again', 'Find out your next favorite anime', 'Get yourself some great anime takes', 'For when you just finished a great anime, but want more', 'TODO: Add more phrases'];

const HeaderText = () => {
    const [currentPhraseIndex, setCurrentPhraseIndex] = useState(0);
    const [transitioning, setTransitioning] = useState(true);

    useEffect(() => {
        const intervalId = setInterval(() => {
            setTransitioning(false);
            setTimeout(() => {
                setTransitioning(true)
                setCurrentPhraseIndex((prevIndex) => (prevIndex + 1) % phrases.length);
            }, 2000);
        }, 10000);

        return () => clearInterval(intervalId);
    }, []);

    return (
        <Fade in={transitioning} timeout={2000}>
            <Typography variant="h2" style={{textAlign: 'center', color: 'white', fontFamily: 'Poppins'}}>{phrases[currentPhraseIndex]}</Typography>
        </Fade>
    );
};

export default HeaderText;
