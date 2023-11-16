import BasicAppBar from './components/AppBar/BasicAppBar';
import { RecAnimePageProvider } from './contexts/recanimepage';
import RecommendByAnime from './pages/RecommendByAnime';

function App() {
  // TODO: Add more pages here (AppBar too) as needed
  return (
    <>
      <BasicAppBar />
      <RecAnimePageProvider>
        <RecommendByAnime />
      </RecAnimePageProvider>
    </>
  );
}



export default App;
