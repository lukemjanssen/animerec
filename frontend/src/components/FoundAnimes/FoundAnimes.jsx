import React from 'react';
import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Box, Divider, Typography } from '@mui/material';
import AnimeCard from '../AnimeCard/AnimeCard';

export default function FoundAnimes() {
    const [animes, setAnimes] = useState([]);

    useEffect(() => {
        axios.get("http://localhost:8080/animecontroller/getrecs?url=https://myanimelist.net/anime/10165/Nichijou/stats?m=all#members")
            .then((response) => {
                setAnimes(response.data);
                setLoading(false);
            })
            .catch((error) => {
                console.log(error);
                setError(error);
            });
    }
    , []);
    return (
        <>
        <Box sx={{ display: 'flex', justifyContent: 'space-around', flexWrap: 'wrap', flexDirection: 'row', mt: "8%" }}>
            {animes.slice(0, 3).map((anime, index) => (
                <AnimeCard key={index} anime={anime} position={index + 1} />
            ))}
        </Box>
        <Box sx={{marginTop:'10%', height:'100%'}}>
            <Box sx={{width:'100%',display:'flex', justifyContent:'center'}}>
                <Box sx={{
                        border: '2px solid #000',
                        borderRadius: '25px',
                        background: "rgb(255,255,255)",
                        background: "radial-gradient(circle, rgba(255,255,255,1) 0%, rgba(218,218,218,1) 80%, rgba(100,100,100,1) 100%)",
                        boxShadow: "rgba(0, 0, 0, 0.2) 0px 60px 40px -7px;",
                        width: '50%',
                    }}>
                    <Typography variant='h5' sx={{display:'flex', justifyContent:'center'}}>
                        Here's other choices that might suit you
                    </Typography>
                </Box>
            </Box>
            <Box className={"seguraLixo"} sx={{ display: 'flex', justifyContent: 'space-around', flexWrap: 'wrap', flexDirection: 'row', alignItems:'center', mt:'5%'}}>
                {animes.slice(3).map((anime, index) => (
                    <AnimeCard key={index} anime={anime} />
                ))}
            </Box>
        </Box>
    </>
    );
};