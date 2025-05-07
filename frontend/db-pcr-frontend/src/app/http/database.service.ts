import { Injectable } from '@angular/core';
import { ReviewStatus, ReviewStatusEntity } from '../interface/review-status';
import { SPRING_URL } from '../service/constant.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DatabaseService {
  private baseUrl = SPRING_URL;

  constructor(private http: HttpClient) {}

  getReviewStatus(
    username: string,
    projectId: string
  ): Observable<ReviewStatusEntity[]> {
    const url = `${this.baseUrl}/get-review-status?username=${username}&projectId=${projectId}`;
    return this.http.get<ReviewStatusEntity[]>(url, {
      withCredentials: true,
    });
  }

  createReviewStatus(
    username: string,
    projectId: string,
    commitSha: string,
    reviewStatus: ReviewStatus
  ): Observable<ReviewStatusEntity> {
    const url = `${this.baseUrl}/create-review-status`;

    const payload: ReviewStatusEntity = {
      username,
      projectId,
      commitSha,
      reviewStatus,
    };

    return this.http.post<ReviewStatusEntity>(url, payload, {
      withCredentials: true,
    });
  }

  updateReviewStatus(
    username: string,
    projectId: string,
    commitSha: string,
    reviewStatus: ReviewStatus
  ): Observable<ReviewStatusEntity> {
    const url = `${this.baseUrl}/update-review-status`;

    const payload: ReviewStatusEntity = {
      username,
      projectId,
      commitSha,
      reviewStatus,
    };

    return this.http.put<ReviewStatusEntity>(url, payload, {
      withCredentials: true,
    });
  }
}
