
public class RecognizeActivity extends ActionBarActivity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;
    private String song = null;
    private List<String> songs = new ArrayList<>();
    MediaPlayer mediaPlayer = new MediaPlayer();

    // The button to select an image
    private Button mButtonSelectImage;
    private Button playButton;
    private Button nextButton;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;


    public void playPause(View view) throws IOException {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void nextSong(View view) throws IOException {
        mediaPlayer.stop();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(songs.get(new Random().nextInt(songs.size())));
        mediaPlayer.prepare();
        playPause(view);
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (e != null) {
                this.e = null;
            } else if (result.size() != 0) {
                Integer count = 0;
                songs.clear();
                Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas faceCanvas = new Canvas(bitmapCopy);
                faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                paint.setColor(Color.RED);
                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                playButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                RecognizeResult r = result.get(0);
                double happiness = r.scores.happiness;
                double neutral = r.scores.neutral;
                double sadness = r.scores.sadness;
                int max = happiness > neutral && happiness > sadness ? 2 : neutral > sadness ? 1 : 0;
                Scanner scan = null;
                try {
                    scan = new Scanner(new File("/storage/emulated/0/songmoods.txt"));
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                if (!scan.equals(null)) {
                    while (scan.hasNextLine()) {
                        String line = scan.nextLine();
                        if (Integer.parseInt(line.substring(0, 1)) == max) {
                            songs.add(line.substring(2));
                        }
                    }
                }
                song = songs.get(new Random().nextInt(songs.size()));
                try {
                    mediaPlayer.setDataSource(song);
                    mediaPlayer.prepare();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            mButtonSelectImage.setEnabled(true);
        }
    }
}
