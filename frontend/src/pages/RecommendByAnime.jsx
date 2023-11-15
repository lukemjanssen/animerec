import * as React from 'react';
import HeaderText from '../components/HeaderText/HeaderText';
import SearchBar from '../components/SearchBar/SearchBar';
import Waves from '../components/Waves/Waves';
import BouncyLoading from '../components/BouncyLoading/BouncyLoading';

import { Box, Button, Grow, Slide } from '@mui/material';
import { useAnimeContext } from '../contexts/recanimepage';

export default function RecommendByAnime() {
    const { loading } = useAnimeContext();

    return (
    <>
        <Box sx={{zIndex:10}}>
            <Box sx={{mt: '5%'}}>
                <HeaderText />
                <Slide direction='up' in={loading} mountOnEnter unmountOnExit timeout={1000}>
                <Box sx={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    alignItems: 'center', 
                    mt: '10%',
                    top: 0, 
                    left: 0, 
                    right: 0, 
                    bottom: 0,
                }}>
                        <BouncyLoading />
                    </Box>
                </Slide>
                <Grow direction='up' in={!loading} mountOnEnter unmountOnExit timeout={1000}>
                    <Box>
                        <SearchBar />
                    </Box>
                </Grow>
            </Box>
        </Box>
        <Box sx={{zIndex: -999}}>
            <Waves />
        </Box>
    </>
  );
}
