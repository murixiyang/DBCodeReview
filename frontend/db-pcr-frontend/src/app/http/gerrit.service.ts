import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SPRING_URL } from '../constant.service';
import { CommitInfo } from '../interface/commit-info';
import { ProjectInfoModel } from '../interface/project-info';

@Injectable({
  providedIn: 'root',
})
export class GerritService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  getProjectList(): Observable<ProjectInfoModel[]> {
    console.log('getProjectList');
    return this.http.get<ProjectInfoModel[]>(
      `${this.baseUrl}/get-project-list`
    );
  }

  getCommitList(): Observable<CommitInfo[]> {
    return this.http.get<CommitInfo[]>(`${this.baseUrl}/get-commit-list`);
  }

  getAnonymousCommitList(): Observable<CommitInfo[]> {
    return this.http.get<CommitInfo[]>(
      `${this.baseUrl}/get-anonymous-commit-list`
    );
  }
}
