export type BookFormat = "PHYSICAL" | "EBOOK" | "AUDIOBOOK";
export type BookStatus = "NOT_STARTED" | "READING" | "FINISHED" | "ON_HOLD";

export interface Book {
  id: string;
  ownerUid: string;
  title: string;
  authors: string[];
  isbn?: string;
  coverImageUrl?: string;
  pageCount?: number;
  currentPage?: number;
  format: BookFormat;
  status: BookStatus;
  genre?: string;
  moodTags?: string[];
  publicationYear?: number;
  personalRating?: number;
  seriesId?: string;
  seriesPosition?: number;
  hardcoverBookId?: string;
  addedAt?: string;
  updatedAt?: string;
}

export interface BookMetadataSuggestion {
  title?: string;
  authors?: string[];
  isbn?: string;
  coverImageUrl?: string;
  pageCount?: number;
  publicationYear?: number;
  genre?: string;
  source?: string;
}

export interface BookFilter {
  genre?: string;
  moodTags?: string[];
  status?: BookStatus;
  format?: BookFormat;
  minPageCount?: number;
  maxPageCount?: number;
  minPublicationYear?: number;
  maxPublicationYear?: number;
  minRating?: number;
  sortBy?: string;
  sortDescending?: boolean;
}

export interface SeriesFollowView {
  seriesId: string;
  seriesName: string;
  nextReleaseTitle?: string;
  nextReleaseDate?: string;
  isCompleted?: boolean | null;
  discarded: boolean;
}

export interface HardcoverSeriesBook {
  title: string;
  coverImageUrl?: string;
  releaseDate?: string;
  position?: number;
}

export interface UserAccount {
  uid: string;
  email: string;
  displayName?: string;
  createdAt?: string;
}
