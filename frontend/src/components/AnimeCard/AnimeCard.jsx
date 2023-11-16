import { useState } from 'react';
import { Typography } from '@mui/material';

import animeDefault from '../../assets/animeDefault.jpg';
import cardBack from '../../assets/cardBack.jpg';
import './card.css'


const AnimeCard = ({ anime, position }) => {
    const [flipped, setFlipped] = useState(false);
    
    if (!anime.image) anime.image = animeDefault; // Source for default 404: https://figuya.com/next-version/static/media/not-found.fae9d144934838d46ba4.jpg

    position = position ?? 'default';

    const sizeFront = {
        1: '1.25',
        2: '1.05',
        3: '1',
        'default': '0.9'
    };

    const sizeBack = {
        1: '1',
        2: '1.15',
        3: '1.17',
        'default': '1.27'
    };

    const toggleFlipped = () => {
        setFlipped(!flipped);
    };

    const frontStyles = {
        width: 'auto',
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-end',
        alignItems: 'center',
        padding: '20px',
        backgroundImage: `linear-gradient(#282C35,
            transparent 20%,
            transparent 80%,
            #282C35), 
            url(${anime.image})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        borderRadius: '25px',
        border: '1px solid #99aab5',
        color: 'white',
        textAlign: 'center',
    };

    const backStyles = {
        width: 'auto',
        height: '100%',
        padding: '20px',
        scale: sizeBack[position] ?? 1.2,
        backgroundImage: `url(${cardBack})`, // Source: https://static.vecteezy.com/system/resources/thumbnails/015/635/615/small/black-perforated-metal-background-metal-texture-steel-carbon-fiber-background-perforated-sheet-metal-vector.jpg
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        borderRadius: '25px',
        border: '1px solid #99aab5',
        color: 'white',
        textAlign: 'center',
    };

    console.log(anime.title, position, position==='default' ? "default" : "top");
    return (
        <div
        className={`flip-card ${flipped ? "flipped" : ""} ${position==='default' ? "default" : "top"}
        `.trim()}
        style={{
            width: "300px",
            height: "452px",
            scale: sizeFront[position] ?? 0.8,
        }}
        >
        <div className="flip-card-inner" onClick={toggleFlipped}>
            <div className="flip-card-front">
                <div className="card-content" style={frontStyles}>
                    {anime.title}
                </div>
            </div>
            <div className="flip-card-back">
                <div className="card-content" style={backStyles}>
                    {anime.synopsis}
                </div>
            </div>
        </div>
    </div>

    );
};
export default AnimeCard;
