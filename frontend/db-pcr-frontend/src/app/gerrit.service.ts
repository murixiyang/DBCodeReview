import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from './constant.service';
import { CommitInfo } from './interface/commit-info';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  getCommitList(): Observable<CommitInfo[]> {
    return this.http.get<CommitInfo[]>(`${this.baseUrl}/get-commit-list`);
  }

  getAnonymousCommitList(): Observable<CommitInfo[]> {
    return this.http.get<CommitInfo[]>(
      `${this.baseUrl}/get-anonymous-commit-list`
    );
  }
}
