import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

import 'diff2html/bundles/css/diff2html.min.css';
import 'highlight.js/styles/github.css';

bootstrapApplication(AppComponent, appConfig).catch((err) =>
  console.error(err)
);
