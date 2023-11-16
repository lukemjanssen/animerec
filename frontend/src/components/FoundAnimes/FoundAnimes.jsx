import React from 'react';
import { Box, Divider, Typography } from '@mui/material';
import AnimeCard from '../AnimeCard/AnimeCard';

export default function FoundAnimes() {
    // TODO: Replace with actual anime data and use a map function to render the cards.
    return (
        <>
            <Box sx={{ display: 'flex', justifyContent: 'space-around', flexWrap: 'wrap', flexDirection: 'row', mt: "8%" }}>
                <AnimeCard anime={{title: "Second best", synopsis: "Sample Synopsis"}} position={2}/>
                <AnimeCard anime={{title: "Best anime", synopsis: "Sample Synopsis"}} position={1}/>
                <AnimeCard anime={{title: "Third best", synopsis: "Sample Synopsis"}} position={3}/>
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
                    <Box sx={{display:'flex', justifyContent:'center'}}>
                        <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                    </Box>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                </Box>
            </Box>
        </>
    );
};