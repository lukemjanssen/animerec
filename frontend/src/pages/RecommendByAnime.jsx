import * as React from 'react';
import HeaderText from '../components/HeaderText/HeaderText';
import SearchBar from '../components/SearchBar/SearchBar';
import Waves from '../components/Waves/Waves';
import BouncyLoading from '../components/BouncyLoading/BouncyLoading';

import { Box, Button, Divider, Grow, Slide } from '@mui/material';
import { useAnimeContext } from '../contexts/recanimepage';
import AnimeCard from '../components/AnimeCard/AnimeCard';
import FoundAnimes from '../components/FoundAnimes/FoundAnimes';

export default function RecommendByAnime() {
    const { loading, toggleLoading, animeList, setAnimeList, selectPhrases } = useAnimeContext();

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
                        <Button variant="contained" color="mal" sx={{ mt: 2, color:'#fff', borderRadius: '30px' }} onClick={()=>{toggleLoading(); selectPhrases('found'); setAnimeList(['a'])}}>DEBUG: Finish search</Button>
                    </Box>
                </Slide>
                <Slide direction='up' in={!loading && !!animeList.length} mountOnEnter unmountOnExit timeout={1000}>
                    <Box sx={{width:'100%'}}>
                        <FoundAnimes />
                    </Box>
                </Slide>
                <Grow direction='up' in={!loading && !animeList.length} mountOnEnter unmountOnExit timeout={1000}>
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
