import React, { useState, useEffect, useRef } from 'react';
import { Fade, Typography } from '@mui/material';

import { useAnimeContext } from '../../contexts/recanimepage';

const HeaderText = () => {
    const { phrases } = useAnimeContext();
    const [currentPhraseIndex, setCurrentPhraseIndex] = useState(0);
    const [transitioning, setTransitioning] = useState(true);
    const phraseLen = useRef(phrases.length);
    phraseLen.current = phrases.length;

    useEffect(() => {
        const intervalId = setInterval(() => {
            setTransitioning(false);
            setTimeout(() => {
                setTransitioning(true)
                setCurrentPhraseIndex((prevIndex) =>(prevIndex + 1) % phraseLen.current);
            }, 2000);
        }, 10000);

        return () => clearInterval(intervalId);
    }, []);

    useEffect(() => {
        phraseLen.current = phrases.length;
        setCurrentPhraseIndex(0);
    }, [phrases]);

    return (
        <Fade in={transitioning} timeout={2000}>
            <Typography variant="h2" style={{textAlign: 'center', color: 'white', fontFamily: 'Poppins'}}>{phrases[currentPhraseIndex]}</Typography>
        </Fade>
    );
};

export default HeaderText;
