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
            <Box sx={{ position: 'absolute', bottom:'-50%', width:'100%' }}> {/* This is a hacky way to make the divider appear at the bottom of the page. */}
                <Divider sx={{
                    "&::before, &::after": {
                        borderColor: "#000",
                    },}}>
                    <Typography variant='h5'>
                        Here's other choices that might suit you
                    </Typography>
                </Divider>
                <Box sx={{ display: 'flex', justifyContent: 'space-around', flexWrap: 'wrap', flexDirection: 'row'}}>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                    <AnimeCard anime={{title: "Sample Title", synopsis: "Sample Synopsis"}}/>
                </Box>
            </Box>
        </>
    );
};