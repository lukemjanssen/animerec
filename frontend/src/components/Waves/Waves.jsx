import waveTop from '../../assets/Waves/wave-top.png';
import waveMid from '../../assets/Waves/wave-mid.png';
import waveBot from '../../assets/Waves/wave-bot.png';
import './waves.css';

// Very cool animation that I got from https://codepen.io/plavookac/pen/QMwObb
export default function Waves() {
    return (
        <>
            <div className="waveWrapper waveAnimation" style={{ position: 'absolute', width: '100%', height: '100%' }}>
                <div className="waveWrapperInner bgTop">
                    <div className="wave waveTop" style={{backgroundImage: `url(${waveTop})`}}></div>
                </div>
                <div className="waveWrapperInner bgMiddle">
                    <div className="wave waveMiddle" style={{backgroundImage: `url(${waveMid})`}}></div>
                </div>
                <div className="waveWrapperInner bgBottom">
                    <div className="wave waveBottom" style={{backgroundImage: `url(${waveBot})`}}></div>
                </div>
            </div>
        </>
    );
}